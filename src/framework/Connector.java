package framework;

public abstract class Connector {

    //string ?
    protected String glue;

    public Connector(String glue) {
        this.glue = glue;
    }

    // Connecter un composant à ce connecteur fe fàçon générique
    public abstract void setComponent(Object component);
}