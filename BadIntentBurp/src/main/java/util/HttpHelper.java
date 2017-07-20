package util;


import burp.IExtensionHelpers;
import burp.IRequestInfo;

import java.util.List;

public class HttpHelper {

    /**
     * BadIntent is recognized if the HTTP header __BadIntent__: Parcel is set.
     * @param iRequestInfo
     * @param isRequest
     * @param helpers
     * @return true iff is BadIntent request
     */
    public static boolean isBadIntentParcelRequest(IRequestInfo iRequestInfo, boolean isRequest, IExtensionHelpers helpers) {
        List<String> headers = iRequestInfo.getHeaders();
        return isRequest && headers.contains("__BadIntent__: Parcel");
    }

    /**
     * BadIntent is recognized if the HTTP header __BadIntent__: Log is set.
     * @param iRequestInfo
     * @param isRequest
     * @param helpers
     * @return true iff is BadIntent log request
     */
    public static boolean isBadIntentLogRequest(IRequestInfo iRequestInfo, boolean isRequest, IExtensionHelpers helpers) {
        List<String> headers = iRequestInfo.getHeaders();
        return isRequest && headers.contains("__BadIntent__: Log");
    }

    /**
     * Returns package name which is contained in HTTP header __BadIntent__.package: <package_name>.
     * @param iRequestInfo
     * @return package name
     */
    public static String getPackageName(IRequestInfo iRequestInfo) {
        for (String header: iRequestInfo.getHeaders()){
            if (header.startsWith("__BadIntent__.package:")){
                return header.split(":")[1].trim();
            }
        } return "UNKNOWN";
    }
}
