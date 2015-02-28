package de.bluekiwi.labs.sio.statistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import moa.classifiers.Classifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.core.InstancesHeader;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import com.thoughtworks.xstream.XStream;

public class TrainClassifierServlet extends HttpServlet {

    private static final long serialVersionUID = -9192022065342984515L;
    
//    Gson gson = new Gson();
    XStream xstream = new XStream();
    
    public void doPost(HttpServletRequest req,
            HttpServletResponse res)
            throws ServletException, IOException {
        
        int projectId = Integer.parseInt(req.getParameter("projectId"));
        String context = req.getParameter("context");
        String contextHash = req.getParameter("contextHash");
        String interfaceVersion = req.getParameter("interfaceVersion");
        String classifier = req.getParameter("classifier");
        String itemName = req.getParameter("itemName");
        String itemValues = req.getParameter("itemValues");
        String featureNames = req.getParameter("featureNames");
        String featureValues = req.getParameter("featureValues");
        
        PrintWriter out = res.getWriter();
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection((String) this.getServletContext().getAttribute("jdbcUrl"));
            
            PreparedStatement modelQueryStatement = con.prepareStatement("INSERT INTO wappu_models (project_id, "
                    + "context, context_hash, interface_version, item, classifier, model) VALUES (?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE model = VALUES(model)");
            ResultSet existingModel = modelQueryStatement.executeQuery("SELECT * FROM "
                    + "wappu_models WHERE project_id = " + projectId + " AND context_hash = '" + contextHash
                    + "' AND interface_version = '" + interfaceVersion + "' AND item = '" + itemName
                    + "' AND classifier = '" + classifier + "'");
            
            Classifier c;
            
            String[] featureNamesArray = featureNames.split(",");
            String[] featureValuesArray = featureValues.split(",");
            String[] itemValuesArray = itemValues.split(",");
            int numInstances = featureValuesArray.length / featureNamesArray.length;
            
            ArrayList<Attribute> attributes = new ArrayList<Attribute>();
            List<String> nominalValues = new ArrayList<String>(3);
            
            nominalValues.add("-1");
            nominalValues.add("0");
            nominalValues.add("1");
            
            for (int i=0; i<featureNamesArray.length; ++i) {
                attributes.add(new Attribute(featureNamesArray[i]));
            }
            
            attributes.add(new Attribute(itemName, nominalValues));
            
            Instances header = new Instances("wappu_interactions", attributes, 0);
            
            header.setClassIndex(header.numAttributes() - 1);
            
            if (existingModel.next()) {
                // model for project ID and usability item already exists
                c = (Classifier) xstream.fromXML(existingModel.getString("model"));
//                c = (Classifier) xstream.fromXML(Helper.decompress(existingModel.getString("model")));
//                c = (Classifier) StoreUtilsKryo.deserializeObject(
//                        Base64.decodeBase64(existingModel.getString("model")), Compression.ZIP_FAST);
            } else {
                // create new model
                c = (Classifier) Class.forName(classifier).newInstance();
                
                c.setModelContext(new InstancesHeader(header));
                c.prepareForUse();
            }
            
            for (int i=0; i<numInstances; ++i) {
                Instance in = new DenseInstance(featureNamesArray.length + 1);
                
                in.setDataset(header);
                
                for (int j=0; j<featureNamesArray.length; ++j) {
                    in.setValue(j, Double.parseDouble(featureValuesArray[j*numInstances+i]));
                }
                
                in.setValue(featureNamesArray.length, itemValuesArray[i]);
                c.trainOnInstance(in);
                
                System.out.println(in.toString());
            }
            
//            byte[] serializedClassifier = StoreUtilsKryo.serializeObject(c, Compression.ZIP_FAST);
//            String serializedClassifier = Helper.compress(xstream.toXML(c));
            String serializedClassifier = xstream.toXML(c);
            
            modelQueryStatement.setInt(1, projectId);
            modelQueryStatement.setString(2, context);
            modelQueryStatement.setString(3, contextHash);
            modelQueryStatement.setString(4, interfaceVersion);
            modelQueryStatement.setString(5, itemName);
            modelQueryStatement.setString(6, classifier);
            modelQueryStatement.setString(7, serializedClassifier);
            modelQueryStatement.executeUpdate();
            
            modelQueryStatement.close();
            con.close();
            out.println("\"success\"");
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("\"SQLException\"");
        } catch (InstantiationException e) {
            e.printStackTrace();
            out.println("\"InstantiationException\"");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            out.println("\"IllegalAccessException\"");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.println("\"ClassNotFoundException\"");
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
