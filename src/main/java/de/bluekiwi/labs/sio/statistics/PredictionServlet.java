package de.bluekiwi.labs.sio.statistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import moa.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import com.thoughtworks.xstream.XStream;

public class PredictionServlet extends HttpServlet {

    private static final long serialVersionUID = -8986985255047369648L;
    
//    Gson gson = new Gson();
    XStream xstream = new XStream();

    public void doPost(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        
        int projectId = Integer.parseInt(req.getParameter("projectId"));
        String interfaceVersion = req.getParameter("interfaceVersion");
        String contextHash = req.getParameter("contextHash");
        String classifier = req.getParameter("classifier");
        String itemName = req.getParameter("itemName");
        String featureNames = req.getParameter("featureNames");
        String featureValues = req.getParameter("featureValues");
        
        PrintWriter out = res.getWriter();
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection((String) this.getServletContext().getAttribute("jdbcUrl"));
            
            Statement modelQueryStatement = con.createStatement();
            ResultSet existingModel = modelQueryStatement.executeQuery("SELECT * FROM "
                    + "wappu_models WHERE project_id = " + projectId + " AND context_hash = '" + contextHash
                    + "' AND interface_version = '" + interfaceVersion + "' AND item = '" + itemName + "' AND classifier = '"
                    + classifier + "'");
            
            Classifier c;
            
            String[] featureNamesArray = featureNames.split(",");
            String[] featureValuesArray = featureValues.split(",");
            int numInstances = featureValuesArray.length / featureNamesArray.length;
            
            ArrayList<Attribute> attributes = new ArrayList<Attribute>();
//          List<String> nominalValues = new ArrayList<String>(3);
          List<String> nominalValues = new ArrayList<String>(2);
          
//          nominalValues.add("-1");
          /*
           * Two nominal values are sufficient if we have an INUIT questionnaire with
           * yes/no answers.
           */
          nominalValues.add("0");
          nominalValues.add("1");
            
            for (int i=0; i<featureNamesArray.length; ++i) {
                attributes.add(new Attribute(featureNamesArray[i]));
            }
            
            attributes.add(new Attribute(itemName, nominalValues));
            
            Instances header = new Instances("wappu_interactions", attributes, 0);
            
            header.setClassIndex(header.numAttributes() - 1);
            
            String result = "[\n";
            
            if (existingModel.next()) {
                c = (Classifier) xstream.fromXML(existingModel.getString("model"));
//                c = (Classifier) xstream.fromXML(Helper.decompress(existingModel.getString("model")));
//                c = (Classifier) StoreUtilsKryo.deserializeObject(
//                        Base64.decodeBase64(existingModel.getString("model")), Compression.ZIP_FAST);
                
                System.out.println("PREDICTION START");
                
                for (int i=0; i<numInstances; ++i) {
                    Instance in = new DenseInstance(featureNamesArray.length + 1);
                    
                    in.setDataset(header);
                    
                    for (int j=0; j<featureNamesArray.length; ++j) {
                        in.setValue(j, Double.parseDouble(featureValuesArray[j*numInstances+i]));
                    }
                    
                    in.setMissing(featureNamesArray.length);
                    
                    double[] prediction = c.getVotesForInstance(in);
                    double[] normalizedPrediction = Arrays.copyOf(prediction, prediction.length);
                    double sum = 0;
                    
                    System.out.println(Arrays.toString(prediction));
                    
                    for (int j=0; j<prediction.length; ++j) {
                        sum += prediction[j];
                    }
                    
                    // prediction is ambiguous (ADJUST ACCORDING TO NUMBER OF NOMINAL VALUES!)
                    if (prediction.length < 2) {
                        System.out.println("predicition is ambiguous");
                        continue;
                    }
                    
                    // all votes are 0.0 => instance is unclassified
                    if (sum == 0.0) {
                        System.out.println("instance is unclassified");
                        continue;
                    }
                    
                    if (sum != 1.0) {
                        double factor = 1.0 / sum;
                        
                        for (int j=0; j<prediction.length; ++j) {
                            normalizedPrediction[j] = prediction[j] * factor;
                        }
                    }
                    
                    result += (result.equals("[\n") ? "" : ",");
                    result += "\n\t{\n";
                    result += ("\t\t\"votesForInstance\": " + Arrays.toString(prediction) + ",\n");
                    result += ("\t\t\"normalizedVotesForInstance\": " + Arrays.toString(normalizedPrediction) + "\n");
                    result += "\t}";
                }
            } else {
                
            }
            
            System.out.println("");
            
            result += "\n]";
            
            modelQueryStatement.close();
            con.close();
            out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("{error: 'SQLException'}");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.println("{error: 'ClassNotFoundException'}");
        }
        
        out.flush();
        out.close();
    }
    
    public void doGet(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        
        doPost(req, res);
    }
    
}
