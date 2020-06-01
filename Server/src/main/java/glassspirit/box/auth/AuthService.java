package glassspirit.box.auth;

import glassspirit.box.logging.SpiritLogger;
import glassspirit.box.model.User;
import glassspirit.box.properties.SpiritProperties;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

public class AuthService {

    private Connection connect;
    private Statement statement;
    private PreparedStatement getUserStatement;
    private PreparedStatement registerUserStatement;
    private PreparedStatement changePasswordStatement;
    private PreparedStatement changeNicknameStatement;
    private ResultSet rs;

    private boolean ready;

    private Logger logger;

    public AuthService() {
        setupAuthService();
    }

    private void setupAuthService() {
        logger = SpiritLogger.getLogger();
        String databaseHost = SpiritProperties.getString("database.host", "localhost");
        int databasePort = SpiritProperties.getInteger("database.port", 3306);
        String databaseName = SpiritProperties.getString("database.name", "spirit_chat");
        String databaseUser = SpiritProperties.getString("database.username", "root");
        String databasePass = SpiritProperties.getString("database.password", "root");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection(
                    "jdbc:mysql://" + databaseHost + ":" + databasePort + "/" + databaseName, databaseUser, databasePass);
            statement = connect.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS accounts(" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(25) NOT NULL UNIQUE," +
                    "password VARCHAR(64) NOT NULL," +
                    "nickname VARCHAR(25));"
            );
            getUserStatement = connect.prepareStatement("SELECT * FROM accounts WHERE username = ?;");
            registerUserStatement = connect.prepareStatement("INSERT INTO accounts (username, password, nickname) VALUES(?, ?, ?);");
            changePasswordStatement = connect.prepareStatement("UPDATE accounts SET password = ? WHERE username = ?;");
            changeNicknameStatement = connect.prepareStatement("UPDATE accounts SET nickname = ? WHERE username = ?;");

            ready = true;
            logger.info("Соединение с базой данных успешно");
        } catch (Exception e) {
            logger.error("Ошибка соединения с базой данных!", e);
        }
    }

    public void reset() {
        logger.info("Перезагрузка соединения с базой данных!");
        ready = false;
        connect = null;
        statement = null;
        rs = null;
        setupAuthService();
    }

    public User getUser(String username) {
        if (!ready) return null;

        try {
            getUserStatement.setString(1, username);
            rs = getUserStatement.executeQuery();
            if (rs.first()) {
                String password = rs.getString("password");
                String nickname = rs.getString("nickname");
                return new User(username, password, nickname);
            }
        } catch (Exception e) {
            logger.error("Ошибка получения юзера из БД", e);
        }
        return null;
    }

    public boolean auth(User user, String password) {
        if (user != null && user.getPassword().equals(SHA256(password))) {
            user.setAuthorized(true);
            return true;
        }
        return false;
    }

    public boolean changePassword(User user, String password) {
        if (!ready) return false;
        if (user == null) return false;
        String hash = SHA256(password);
        try {
            changePasswordStatement.setString(1, hash);
            changePasswordStatement.setString(2, user.getUsername());
            changePasswordStatement.executeUpdate();
            user.setPassword(hash);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка смены пароля юзера " + user.getUsername(), e);
        }
        return false;
    }

    public boolean changeNickname(User user, String nickname) {
        if (!ready) return false;
        if (user == null) return false;
        try {
            changeNicknameStatement.setString(1, nickname);
            changeNicknameStatement.setString(2, user.getUsername());
            changeNicknameStatement.executeUpdate();
            user.setNickname(nickname);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка смены никнейма юзера " + user.getUsername(), e);
        }
        return false;
    }

    public boolean register(String username, String password, String nickname) {
        if (!ready) return false;
        if (getUser(username) != null) return false;
        if (nickname == null || nickname.isEmpty())
            nickname = username;
        try {
            registerUserStatement.setString(1, username);
            registerUserStatement.setString(2, SHA256(password));
            registerUserStatement.setString(3, nickname);
            registerUserStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Ошибка регистрации юзера " + username, e);
        }
        return false;
    }

    private String SHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
