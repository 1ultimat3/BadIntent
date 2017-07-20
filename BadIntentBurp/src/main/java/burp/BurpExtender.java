package burp;

import controller.BadIntentMessageEditorTab;
import listener.ColoringListener;


public class BurpExtender implements IBurpExtender, IMessageEditorTabFactory {

	private IBurpExtenderCallbacks callbacks;
	private BadIntentMessageEditorTab badIntentMessageEditorTab;

	@Override
	public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
		this.callbacks = callbacks;
		this.callbacks.setExtensionName("BadIntent");

		this.callbacks.registerMessageEditorTabFactory(this);
		this.callbacks.registerProxyListener(new ColoringListener(callbacks));
		this.callbacks.addSuiteTab(new ActiveAppsTab());

	}

	@Override
	public IMessageEditorTab createNewInstance(final IMessageEditorController controller, final boolean editable) {
		this.badIntentMessageEditorTab = new BadIntentMessageEditorTab(this.callbacks, editable);
		return badIntentMessageEditorTab;

	}

}
