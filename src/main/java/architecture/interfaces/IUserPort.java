package architecture.interfaces;

public interface IUserPort {
    boolean login(String username, String password);
    void register(String username, String password);
}
