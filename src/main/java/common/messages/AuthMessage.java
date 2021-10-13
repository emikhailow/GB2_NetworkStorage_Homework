package common.messages;

public class AuthMessage extends AbstractMessage {

    private String password;
    private String message;
    private boolean result;

    public AuthMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public boolean isResult() {
        return result;
    }

    public String getPassword() {
        return password;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getLogin() {
        return super.getLogin();
    }

    public void setLogin(String login) {
        super.setLogin(login);
    }
}
