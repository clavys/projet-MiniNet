package framework;

public abstract class Component {
    protected String name;

    public Component(String name) {
        this.name = name;
    }

    protected void printLog(String message) {
        System.out.println("[" + this.name + "] " + message);
    }
}