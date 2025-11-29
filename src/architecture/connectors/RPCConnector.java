package architecture.connectors;

import framework.Connector;
import architecture.interfaces.IUserPort;
import architecture.interfaces.IMessagePort;

public class RPCConnector extends Connector {

    private IUserPort userPort;
    private IMessagePort msgPort;

    public RPCConnector() {
        super("RPC Call-Return");
    }

    @Override
    public void setComponent(Object component) {
        if (component instanceof IUserPort) {
            this.userPort = (IUserPort) component;
        }
        // Si on branche un MessageService
        else if (component instanceof IMessagePort) {
            this.msgPort = (IMessagePort) component;
        }
    }

    // Services Auth existants
    public boolean callLogin(String u, String p) { return userPort.login(u, p); }
    public void callRegister(String u, String p) { userPort.register(u, p); }

    // Services Message
    public void callSendMessage(String from, String to, String content) {
        if (msgPort != null) {
            printLog("APPEL RPC -> SendMessage...");
            msgPort.sendMessage(from, to, content);
        }
    }

    public String callCheckMessages(String user) {
        if (msgPort != null) {
            return msgPort.checkMessages(user);
        }
        return "";
    }

    private void printLog(String msg) { System.out.println("   [RPC] " + msg); }
}