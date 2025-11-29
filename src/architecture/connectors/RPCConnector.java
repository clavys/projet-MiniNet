package architecture.connectors;

import framework.Connector;
import architecture.interfaces.IUserPort;

public class RPCConnector extends Connector {

    private IUserPort componentPort;

    public RPCConnector() {
        super("RPC Call-Return");
    }

    @Override
    public void setComponent(Object component) {
        if (component instanceof IUserPort) {
            this.componentPort = (IUserPort) component;
        }
    }

    // --- Services exposés au Client ---

    public boolean callLogin(String user, String pass) {
        printLog("APPEL RPC -> Login...");
        return componentPort.login(user, pass);
    }

    public void callRegister(String user, String pass) {
        printLog("APPEL RPC -> Register...");
        componentPort.register(user, pass);
    }

    // Simule un délai réseau (optionnel, pour faire "vrai")
    private void printLog(String msg) {
        System.out.println("   [RPC-Transport] " + msg);
    }
}