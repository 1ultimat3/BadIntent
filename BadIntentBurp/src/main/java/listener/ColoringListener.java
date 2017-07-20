package listener;

import burp.*;
import util.HttpHelper;


public class ColoringListener implements IProxyListener {

    protected IBurpExtenderCallbacks callbacks;

    public ColoringListener(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void processProxyMessage(boolean isRequest, IInterceptedProxyMessage iInterceptedProxyMessage) {
        IHttpRequestResponse messageInfo = iInterceptedProxyMessage.getMessageInfo();
        IExtensionHelpers helpers = callbacks.getHelpers();
        IRequestInfo iRequestInfo = helpers.analyzeRequest(messageInfo.getRequest());
        if (HttpHelper.isBadIntentParcelRequest(iRequestInfo, isRequest, helpers)) {
            updateColor(messageInfo, iRequestInfo, "green");
            ActiveAppsTab.addOpenTask(iInterceptedProxyMessage.getClientIpAddress(),
                    iInterceptedProxyMessage.getMessageInfo().getHttpService().getPort());
        } else if (HttpHelper.isBadIntentLogRequest(iRequestInfo, isRequest, helpers)) {
            updateColor(messageInfo, iRequestInfo, "gray");
        }
    }

    public void updateColor(IHttpRequestResponse messageInfo, IRequestInfo iRequestInfo, String color) {
        messageInfo.setHighlight(color);
        messageInfo.setComment(HttpHelper.getPackageName(iRequestInfo));
    }
}
