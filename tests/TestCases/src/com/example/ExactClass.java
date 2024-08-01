package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.newrelic.api.agent.Trace;

public class ExactClass {
	@Trace
	public void makeExternalCall() {

		try {
			// Delay of 1 seconds
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Simulate a database or external call
		System.out.println("Making external call in ExactClass");

		// Database connection details
		String url = "jdbc:mysql://localhost:3306/studentdatabase";
		String user = "root";
		String password = "";

		try (Connection connection = DriverManager.getConnection(url, user, password);
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT * FROM students")) {

			while (resultSet.next()) {
				System.out.println("Student ID: " + resultSet.getInt("id"));
				System.out.println("Student Name: " + resultSet.getString("name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ExactClass exactClass = new ExactClass();
		exactClass.makeExternalCall();
	}
}