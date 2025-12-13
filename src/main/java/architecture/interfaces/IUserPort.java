package architecture.interfaces;

public interface IUserPort {
    boolean login(String username, String password);
    boolean register(String username, String password);
}
