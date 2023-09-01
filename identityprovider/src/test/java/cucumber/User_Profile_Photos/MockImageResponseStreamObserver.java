package cucumber.User_Profile_Photos;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;

public class MockImageResponseStreamObserver implements StreamObserver<FileUploadStatusResponse> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private StreamObserver<UploadUserProfilePhotoRequest> requestObserver;
    private List<UploadUserProfilePhotoRequest> requestChunks;
    int numChunks;
    int currentChunk = 0;


    @Override
    public void onNext(FileUploadStatusResponse status) {
        switch (status.getStatusValue()) {
            case FileUploadStatus.FAILED_VALUE -> logger.error("Transfer failed");
            case FileUploadStatus.IN_PROGRESS_VALUE -> {
                if (currentChunk < numChunks) {
                    logger.info("Sending next chunk: " + currentChunk);
                    requestObserver.onNext(requestChunks.get(currentChunk++));
                } else {
                    logger.info("Sent all image chunks calling onComplete() for server");
                    requestObserver.onCompleted();
                }
            }
            case FileUploadStatus.PENDING_VALUE -> {
                if (currentChunk == 1) {
                    logger.info("Sending next chunk: " + currentChunk);
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


    @Override
    public void onError(Throwable throwable) {
        logger.error("Image transfer failure for user "  +
                requestChunks.get(0).getMetaData().getUserId() +
                ":\n" + throwable.getMessage());
    }


    @Override
    public void onCompleted() {
        logger.info("Image transfer successful for user " + requestChunks.get(0).getMetaData().getUserId());
    }


    public void initialise(StreamObserver<UploadUserProfilePhotoRequest> requestObserver) {
        this.requestObserver = requestObserver;
    }


    public void sendImage(List<UploadUserProfilePhotoRequest> requestChunks) {
        this.requestChunks = requestChunks;
        this.numChunks = requestChunks.size();
        requestObserver.onNext(requestChunks.get(currentChunk++));
    }
}