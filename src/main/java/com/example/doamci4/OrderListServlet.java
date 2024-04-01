package com.example.doamci4;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@WebServlet(name = "orderServlet", value = "/odabrana-jela")
public class OrderListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!validatePassword(request.getParameter("password"))) {
            response.sendRedirect("invalid-password.html");
            return;
        }

        List<Map<String, String>> allSelectedMeals = getAllSelectedMealsFromSessions(request);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>Lista porudzbina</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Lista porudzbina po danima:</h1>");

        if (allSelectedMeals.isEmpty()) {
            out.println("<p>Trenutno nema porudzbina.</p>");
        } else {
            Map<String, Map<String, Integer>> dailyMeals = new HashMap<>();

            for (Map<String, String> mealMap : allSelectedMeals) {
                for (Map.Entry<String, String> entry : mealMap.entrySet()) {
                    String day = entry.getKey();
                    String meal = entry.getValue();

                    if (!dailyMeals.containsKey(day)) {
                        dailyMeals.put(day, new HashMap<>());
                    }

                    Map<String, Integer> dayMeals = dailyMeals.get(day);
                    dayMeals.put(meal, dayMeals.getOrDefault(meal, 0) + 1);
                }
            }

            for (Map.Entry<String, Map<String, Integer>> entry : dailyMeals.entrySet()) {
                String day = entry.getKey();
                Map<String, Integer> dayMeals = entry.getValue();
                out.println("<h2>" + day + "</h2>");
                out.println("<ul>");
                for (Map.Entry<String, Integer> mealEntry : dayMeals.entrySet()) {
                    String meal = mealEntry.getKey();
                    int quantity = mealEntry.getValue();
                    out.println("<li>" + meal + " : " + quantity + "</li>");
                }
                out.println("</ul>");
            }
        }

        out.println("<form action=\"/odabrana-jela\" method=\"post\">");
        out.println("<input type=\"submit\" value=\"Restart\">");
        out.println("</form>");

        out.println("</body>");
        out.println("</html>");
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       synchronized (this){
           ((List<String>)request.getSession().getServletContext().getAttribute("mealsSelected")).removeAll(((List<String>)request.getSession().getServletContext().getAttribute("mealsSelected")));
           ((List<Map<String,String>>)request.getSession().getServletContext().getAttribute("allMeals")).removeAll(((List<Map<String,String>>)request.getSession().getServletContext().getAttribute("allMeals")));
       }
//        response.sendRedirect(request.getContextPath() + "/odabrana-jela");
    }
    private boolean validatePassword(String password) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("password.txt").getFile());
        List<String> passwords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                passwords.add(line);
            }
        }
        return passwords.contains(password);
    }


    private List<Map<String, String>> getAllSelectedMealsFromSessions(HttpServletRequest request) {
        List<Map<String, String>> v =(List<Map<String,String>>)request.getSession().getServletContext().getAttribute("allMeals");
        return  v;
    }

}

