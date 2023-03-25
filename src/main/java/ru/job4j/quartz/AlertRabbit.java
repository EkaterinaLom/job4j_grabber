package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static Properties read() {
        final Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static Connection init(Properties properties) throws SQLException,
            ClassNotFoundException {
        Class.forName(properties.getProperty("driver-class-name"));
        var url = properties.getProperty("url");
        var login = properties.getProperty("username");
        var password = properties.getProperty("password");
        return DriverManager.getConnection(url, login, password);
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Properties actualProperties = read();
        var interval = Integer.parseInt(actualProperties.getProperty("rabbit.interval"));
        var intervalSleep = Integer.parseInt(actualProperties.getProperty("rabbit.sleep"));
        try (Connection connection = init(actualProperties)) {
            try {
                List<Long> store = new ArrayList<>();
                var scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                var data = new JobDataMap();
                data.put("connection", connection);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(interval)
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(intervalSleep);
                scheduler.shutdown();
            } catch (Exception se) {
                se.printStackTrace();
            }
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap()
                    .get("connection");
            try (PreparedStatement statement = connection
                    .prepareStatement("insert into rabbit(created_date) values(?)")) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
