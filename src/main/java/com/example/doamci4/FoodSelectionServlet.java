package com.example.doamci4;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.util.*;

@WebServlet(name = "foodServlet", value = "/food-selection")
public class FoodSelectionServlet extends HttpServlet {
    private Map<String, List<String>> availableMeals = new HashMap<>();

    @Override
    public void init() throws ServletException {
        List<Map<String,String>> l = new ArrayList<>();
        this.getServletContext().setAttribute("allMeals",l);
        List<String> l2 = new ArrayList<>();
        this.getServletContext().setAttribute("mealsSelected",l2);
        String[] daysOfWeek = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak", "subota", "nedelja"};
        for (String day : daysOfWeek) {
            try {
                availableMeals.put(day, loadMealsFromFile(day + ".txt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();

        if (((List<String>)session.getServletContext().getAttribute("mealsSelected")).contains(session.getId())) {
            Map<String,String> selectedMeals = (Map<String,String>) session.getAttribute("selectedMeals");
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            out.println("<title>Odabrana jela</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Odabrana jela:</h1>");
            out.println("<ul>");
            for (String day : selectedMeals.keySet()) {
                out.println("<li>" + day+":"+selectedMeals.get(day) + "</li>");
            }
            out.println("</ul>");
            out.println("</body>");
            out.println("</html>");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<title>Odabir jela</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Odaberite jelo za svaki dan u nedelji:</h1>");
        out.println("<form action=\"food-selection\" method=\"post\">");

        for (String day : availableMeals.keySet()) {
            out.println("<h3>" + day + "</h3>");
            out.println("<select name=\"" + day + "\">");
            for (String meal : availableMeals.get(day)) {
                out.println("<option>" + meal + "</option>");
            }
            out.println("</select>");
        }

        out.println("<button type=\"submit\">Potvrdi</button>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        if (((List<String>)session.getServletContext().getAttribute("mealsSelected")).contains(session.getId())) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('Jela su već odabrana u ovoj sesiji.')</script>");
            return;
        }

        session.setAttribute("selectedMeals", extractSelectedMeals(request));

        synchronized (this){
            ((List<String>) session.getServletContext().getAttribute("mealsSelected")).add(session.getId());
        }

        synchronized (this){
            ((List<Map<String,String>>)session.getServletContext().getAttribute("allMeals")).add((Map<String, String>) session.getAttribute("selectedMeals"));
        }

        response.sendRedirect("confirmation.html");
    }

    private List<String> loadMealsFromFile(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());

        List<String> meals = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                meals.add(line);
            }
        }
        return meals;
    }

    private Map<String, String> extractSelectedMeals(HttpServletRequest request) {
        Map<String, String> selectedMeals = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String day = parameterNames.nextElement();
            String meal = request.getParameter(day);
            selectedMeals.put(day, meal);
        }
        return selectedMeals;
    }
}
