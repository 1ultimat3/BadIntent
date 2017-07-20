package controller;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorTab;
import burp.IRequestInfo;
import com.google.common.primitives.Bytes;
import com.google.gson.GsonBuilder;
import dao.ParcelOperationDAO;
import dao.ParcelPoolElementDAO;
import ui.ParcelPanel;
import ui.ParcelTab;
import util.HttpHelper;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


/**
 * This tab is used as a controller for the ParcelEditor view;
 * there it is possible to edit the received actions and values.
 */
public class BadIntentMessageEditorTab implements IMessageEditorTab {

	private IBurpExtenderCallbacks callbacks;
	private ParcelPanel overview;
	private ParcelTab tab;
	private boolean editable;

    private IRequestInfo requestInfo;
    private byte[] content;
    private ParcelPoolElementDAO parcelPoolElementDao;


    public BadIntentMessageEditorTab(final IBurpExtenderCallbacks callbacks, final boolean editable) {
		this.callbacks = callbacks;
		this.overview = new ParcelPanel();
		this.tab = new ParcelTab(overview);
		this.editable = editable;
    }

	@Override
	public String getTabCaption() {
		return "ParcelEditor";
	}

	@Override
	public Component getUiComponent() {
        return tab;
	}

	@Override
	public boolean isEnabled(final byte[] content, final boolean isRequest) {
        IExtensionHelpers helpers = callbacks.getHelpers();
        return HttpHelper.isBadIntentParcelRequest(helpers.analyzeRequest(content), isRequest, helpers);
	}

    @Override
	public void setMessage(final byte[] content, final boolean isRequest) {
        this.content = content;
	    if (content == null) return;
        requestInfo = callbacks.getHelpers().analyzeRequest(content);
        int bodyStart = requestInfo.getBodyOffset();
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(content, bodyStart, content.length);
        InputStreamReader reader = new InputStreamReader(byteInputStream);
        parcelPoolElementDao = new GsonBuilder().create().fromJson(reader, ParcelPoolElementDAO.class);
        overview.setParcelOperations(parcelPoolElementDao.operations);
        overview.rebuild();
    }

	@Override
	public byte[] getMessage() {
        List<ParcelOperationDAO> parcelOperationDaos = overview.retrievedParcelOperations();
        parcelPoolElementDao.operations = parcelOperationDaos;
        String responseBody = new GsonBuilder().create().toJson(parcelPoolElementDao);
        int bodyStart = requestInfo.getBodyOffset();
        byte[] header = Arrays.copyOfRange(content, 0, bodyStart);
        return Bytes.concat(header, responseBody.getBytes());
	}

	@Override
	public boolean isModified() {
        return overview.isModified();
	}

	@Override
	public byte[] getSelectedData() {
        return tab.getSelectedData();
	}

}
