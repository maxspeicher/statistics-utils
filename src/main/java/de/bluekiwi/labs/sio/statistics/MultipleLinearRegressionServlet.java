package de.bluekiwi.labs.sio.statistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class MultipleLinearRegressionServlet extends HttpServlet {

    private static final long serialVersionUID = 6796896451292023521L;

    public void doGet(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        doPost(req,res);
    }
    
    public void doPost(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {

        Enumeration<String> attributes = req.getParameterNames();
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        ArrayList<String[]> rawValues = new ArrayList<String[]>();
        
        while (attributes.hasMoreElements()) {
            String attributeName = attributes.nextElement();
            String attribute = req.getParameter(attributeName);
            
            attributeList.add(new Attribute("data['" + attributeName + "']"));
            rawValues.add(attribute.split(","));
        }
        
        Instances data = new Instances("interaction_features", attributeList, rawValues.get(0).length);
        
        for (int i=0; i<rawValues.get(0).length; ++i) {
            Instance in = new DenseInstance(rawValues.size());
            
            for (int j=0; j<rawValues.size(); ++j) {
                in.setValue(j, Double.parseDouble(rawValues.get(j)[i]));
            }
            
            data.add(in);
        }
        
        for (int i=0; i<data.numAttributes(); ++i) {
            if (!data.attribute(i).name().contains("_")) {
                data.setClassIndex(i);
                break;
            }
        }
        
        LinearRegression reg = new LinearRegression();
        
        try {
            reg.buildClassifier(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        PrintWriter out = res.getWriter();
        out.println("{");
        out.println("\t\"model\": \"" + reg.toString().replace("\n", "\\n") + "\",");
        
        try {
            Evaluation eval = new Evaluation(data);
            eval.evaluateModel(reg, data);
            
            out.println("\t\"correlationCoefficient\": " + eval.correlationCoefficient());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        out.println("}");
        out.flush();
        out.close();
    }

}
