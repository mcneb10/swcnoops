package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class PlayerDatasourceImpl implements PlayerDataSource {
    public PlayerDatasourceImpl() {
    }

    @Override
    public void initOnStartup() {
        checkAndPrepareDB();
    }

    public void checkAndPrepareDB() {
        try {
            try (Connection connection = getConnection()) {
                try (Statement stmt = connection.createStatement()) {
                    String sql = getCreatePlayerDBSql();
                    stmt.executeUpdate(sql);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to check player DB " + ex);
        }
    }

    private String getCreatePlayerDBSql() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(ServiceFactory.instance().getConfig().playerCreatePlayerDBSqlResource);

        String content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return content;
    }

    private Connection getConnection() throws SQLException {
        String url = ServiceFactory.instance().getConfig().playerSqliteDB;
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }

    @Override
    public Player loadPlayer(String playerId) {
        final String sql = "SELECT id, name " +
                             "FROM Player p WHERE p.id = ?";

        Player player = null;
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, playerId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        player = new Player(rs.getString("id"));
                        player.setName(rs.getString("name"));
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to check player DB");
        }

        return player;
    }
}
