package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;

public class TimeService {

    /** To hide the implicit public constructor */
    private TimeService() {/* hide the public constructor*/}

    /**
     * Gets the current time as a protobuf timestamp
     */
    public static Timestamp getTimeStamp() {
        long millis = System.currentTimeMillis();

        return Timestamp.newBuilder().setSeconds(millis / 1000)
                .setNanos((int) ((millis % 1000) * 1000000)).build();
    }



}
