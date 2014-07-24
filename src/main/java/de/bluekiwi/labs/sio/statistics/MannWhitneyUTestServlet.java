package de.bluekiwi.labs.sio.statistics;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

public class MannWhitneyUTestServlet extends HttpServlet {

    private static final long serialVersionUID = -3403829246813132187L;
    
    public void doPost(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        
        String x = req.getParameter("x");
        String y = req.getParameter("y");
        
        String[] xStringArr = x.split(",");
        String[] yStringArr = y.split(",");
        
        double[] xNumArr = new double[xStringArr.length];
        double[] yNumArr = new double[yStringArr.length];
        
        for (int i=0; i<xStringArr.length; ++i) {
            xNumArr[i] = Double.parseDouble(xStringArr[i]);
        }
        
        for (int i=0; i<yStringArr.length; ++i) {
            yNumArr[i] = Double.parseDouble(yStringArr[i]);
        }
        
        MannWhitneyUTest mwu = new MannWhitneyUTest();
        
        double p = mwu.mannWhitneyUTest(xNumArr, yNumArr);
        
        PrintWriter out = res.getWriter();
        
        out.println(p);
        out.flush();
        out.close();
    }
    
    public void doGet(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        doPost(req, res);
    }

}
