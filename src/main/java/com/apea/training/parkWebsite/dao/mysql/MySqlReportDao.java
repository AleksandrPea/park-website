package com.apea.training.parkWebsite.dao.mysql;

import com.apea.training.parkWebsite.connection.DaoConnection;
import com.apea.training.parkWebsite.connection.mysql.MySqlTransactionHelper;
import com.apea.training.parkWebsite.dao.DaoException;
import com.apea.training.parkWebsite.dao.ReportDao;
import com.apea.training.parkWebsite.domain.Report;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MySqlReportDao implements ReportDao {

    MySqlReportDao() {
    }

    @Override
    public void create(Report report) {
        String sqlStatement = "INSERT INTO report (comment, imgPath, taskId) VALUES (?, ?, ?)";
        try (DaoConnection connection = MySqlTransactionHelper.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sqlStatement,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, report.getComment());
            statement.setString(2, report.getImgPath());
            statement.setInt(3, report.getTaskId());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new DaoException("Creating report failed: no rows affected.");
            }
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new DaoException("Creating report failed: no id obtained.");
            }
            Integer id = generatedKeys.getInt(1);
            report.setId(id);
            generatedKeys.close();
            statement.close();
        } catch (SQLException e) {
            throw new DaoException("Can't create report", e);
        }
    }

    @Override
    public Report getById(Integer id) {
        String sqlStatement = "SELECT * FROM report WHERE id = ?";
        Report report;
        try (DaoConnection connection = MySqlTransactionHelper.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sqlStatement);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                throw new DaoException("Report with id " + id + " doesn't exist");
            }
            String comment = resultSet.getString("comment");
            String imgPath = resultSet.getString("imgPath");
            long creationDate = resultSet.getTimestamp("creationDate").getTime();
            Integer taskId = resultSet.getInt("taskId");
            report = new Report.Builder().setId(id).setComment(comment).setImgPath(imgPath)
                        .setCreationDate(Instant.ofEpochMilli(creationDate)).setTaskId(taskId).build();
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new DaoException("Can't get report", e);
        }
        return report;
    }

    @Override
    public void delete(Report report) {
        String sqlStatement = "DELETE FROM report WHERE id = ?";
        try (DaoConnection connection = MySqlTransactionHelper.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sqlStatement);
            statement.setInt(1, report.getId());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new DaoException("Deleting report failed: no rows affected.");
            }
            statement.close();
        } catch (SQLException e) {
            throw new DaoException("Can't delete report", e);
        }
    }

    @Override
    public List<Report> getAllOn(Integer taskId) {
        String sqlStatement = "SELECT * FROM report WHERE taskId = ?";
        List<Report> reports = new ArrayList<>();
        try (DaoConnection connection = MySqlTransactionHelper.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sqlStatement);
            statement.setInt(1, taskId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String comment = resultSet.getString("comment");
                String imgPath = resultSet.getString("imgPath");
                long creationDate = resultSet.getTimestamp("creationDate").getTime();
                Report report = new Report.Builder().setId(id).setComment(comment).setImgPath(imgPath)
                        .setCreationDate(Instant.ofEpochMilli(creationDate)).setTaskId(taskId).build();
                reports.add(report);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new DaoException("Can't get all reports on task with id "+taskId, e);
        }
        return reports;
    }
}