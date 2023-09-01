package nz.ac.canterbury.seng302.portfolio.service.grpc;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Defines the StreamObserver<FileUploadStatusResponse> implementation used by the UserAccountsClientService for
 * uploading images.
 */
public class ImageResponseStreamObserver implements StreamObserver<FileUploadStatusResponse> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private StreamObserver<UploadUserProfilePhotoRequest> requestObserver;
    private List<UploadUserProfilePhotoRequest> requestChunks;
    int numChunks;
    int currentChunk = 0;

    /**
     * Takes the response from the previous chunks sending to the server, and takes the correct next action. That is:
     * <br> - if the status was FAILED a message is logged and the upload is scrapped.
     * <br> - if the status was IN_PROGRESS, the client either sends the next chunk, or if no chunks are left calls
     * onComplete on the requestObserver.
     * <br> - if the status was PENDING, the client checks only metadata is sent and send the next chunk.
     * <br> - if the status was SUCCESS, onComplete is called on this to finish the cycle
     * <br>
     *
     * @param status - the FileUploadStatusResponse sent by the server side after successful reception of previous chunk
     */
    @Override
    public void onNext(FileUploadStatusResponse status) {
        switch (status.getStatusValue()) {
            case FileUploadStatus.FAILED_VALUE -> logger.error("Transfer failed");
            case FileUploadStatus.IN_PROGRESS_VALUE -> {
                if (currentChunk < numChunks) {
                    logger.info("Sending next chunk: {}", currentChunk);
                    requestObserver.onNext(requestChunks.get(currentChunk++));
                } else {
                    logger.info("Sent all image chunks calling onComplete() for server");
                    requestObserver.onCompleted();
                }
            }
            case FileUploadStatus.PENDING_VALUE -> {
                if (currentChunk == 1) {
                    logger.info("Sending next chunk: {}", currentChunk);
                    requestObserver.onNext(requestChunks.get(currentChunk++));
                } else {
                    logger.error("Got code PENDING but expected code IN_PROGRESS");
                    requestObserver.onError(new FileNotFoundException("Client Got code PENDING but expected code IN_PROGRESS"));
                }
            }
            case FileUploadStatus.SUCCESS_VALUE -> this.onCompleted();
            default -> onError(new FileNotFoundException("Invalid response received"));
        }

    }

    /**
     * Prints an error message and cancels the image transfer.
     * <br>
     *
     * @param throwable - the error that occurred
     */
    @Override
    public void onError(Throwable throwable) {
        logger.error("Image transfer failure for user {} :\n {}", requestChunks.get(0).getMetaData().getUserId(), throwable.getMessage());
    }

    /**
     * Logs that the file transfer was successful
     */
    @Override
    public void onCompleted() {
        logger.info("Image transfer successful for user {}", requestChunks.get(0).getMetaData().getUserId());
    }

    /**
     * Sets the request observer for the communication.
     * <br>
     *
     * @param requestObserver - used to make the calls and tell the Server what to do.
     */
    public void initialise(StreamObserver<UploadUserProfilePhotoRequest> requestObserver) {
        this.requestObserver = requestObserver;
    }

    /**
     * Starts the image transfer, called once the request observer has been set.
     * <br>
     *
     * @param requestChunks - a List of UploadUserProfilePhotoRequest chunks that collectively make up an image.
     */
    public void sendImage(List<UploadUserProfilePhotoRequest> requestChunks) {
        this.requestChunks = requestChunks;
        this.numChunks = requestChunks.size();
        requestObserver.onNext(requestChunks.get(currentChunk++));
    }

}
