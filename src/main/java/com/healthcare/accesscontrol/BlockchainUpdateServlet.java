package com.healthcare.accesscontrol;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;

@WebServlet(urlPatterns = {"/health", "/access-decision", "/update-blockchain"})
public class BlockchainUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // Local file path to save the blockchain log (adjust the path as needed)
    private static final String LOCAL_BLOCKCHAIN_FILE = "C:\\TeamData\\newblockchainlog.txt";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/health".equals(servletPath)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("OK");
            System.out.println("Health check invoked; returning OK.");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "GET method not supported for this endpoint");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/access-decision".equals(servletPath)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("true");
            System.out.println("Access decision endpoint invoked; returning true.");
        } else if ("/update-blockchain".equals(servletPath)) {
            try (InputStream in = request.getInputStream()) {
                File localFile = new File(LOCAL_BLOCKCHAIN_FILE);
                File parentDir = localFile.getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                } else {
                    System.out.println("Directory verified/created: " + parentDir.getAbsolutePath());
                }
                Files.copy(in, Paths.get(LOCAL_BLOCKCHAIN_FILE), StandardCopyOption.REPLACE_EXISTING);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Blockchain log updated successfully.");
                System.out.println("Blockchain log downloaded and updated at " + LOCAL_BLOCKCHAIN_FILE);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Error updating blockchain log: " + e.getMessage());
                System.err.println("Error updating blockchain log: " + e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "POST method not supported for this endpoint");
        }
    }
}


































/*package com.healthcare.accesscontrol;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@WebServlet(urlPatterns = {"/health", "/access-decision", "/update-blockchain"})
public class BlockchainUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Local file path where the blockchain log will be saved on the team member's laptop.
    private static final String LOCAL_BLOCKCHAIN_FILE = "C:\\TeamData\\newblockchainlog.txt";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle Health Check endpoint: GET /health
        String servletPath = request.getServletPath();
        if ("/health".equals(servletPath)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("OK");
            System.out.println("Health check invoked; returning OK.");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "GET method not supported for this endpoint");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Determine endpoint by the servlet path
        String servletPath = request.getServletPath();
        if ("/access-decision".equals(servletPath)) {
            // Access Decision: always grant access (for testing)
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("true");
            System.out.println("Access decision endpoint invoked; returning true.");
        } else if ("/update-blockchain".equals(servletPath)) {
            // Blockchain Update: receive the file update and save it locally.
            try (InputStream in = request.getInputStream()) {
                // Ensure that the destination directory exists.
                File localFile = new File(LOCAL_BLOCKCHAIN_FILE);
                File parentDir = localFile.getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                } else {
                    System.out.println("Directory verified/created: " + parentDir.getAbsolutePath());
                }
                
                // Write the incoming file bytes to the local file.
                Files.copy(in, Paths.get(LOCAL_BLOCKCHAIN_FILE), StandardCopyOption.REPLACE_EXISTING);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Blockchain log updated successfully.");
                System.out.println("Blockchain log downloaded and updated at " + LOCAL_BLOCKCHAIN_FILE);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Error updating blockchain log: " + e.getMessage());
                System.err.println("Error updating blockchain log: " + e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "POST method not supported for this endpoint");
        }
    }
}
*/