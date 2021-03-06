package iteration2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Random;

public class JSONHandler {
    
    private Dataset dataset;
    
    private int nLabelAssignments=0;
    private int nUniqueLabelAssignments=0;
    private int nUniqueUsers=0;

    Random rand = new Random();

    public void readConfigFile(){
        List<User> userList = new ArrayList<>(); //creating userList to append
        JSONParser jsonParser = new JSONParser();

        try(FileReader reader = new FileReader("./iteration2/config.json")){
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;

            long currentDatasetId=(long) jsonObject.get("currentDatasetId");
            JSONArray datasets = (JSONArray) jsonObject.get("datasets");
            datasets.forEach(entry->{
                JSONObject dataset = (JSONObject) entry;
                long nooffusers = (long) dataset.get("number_of_users");
                long datasetId=(long) dataset.get("dataset_id");
                String datasetPath=(String) dataset.get("path");
                if(currentDatasetId==datasetId){
                    readDataset(datasetPath);
                    this.dataset.setNumberofUsers(nooffusers);
                }
            });

            
            JSONArray users = (JSONArray) jsonObject.get("users");
            long chosenRandomly[]= new long[(int)this.dataset.getNumberofUsers()];
            for(int i=0; i<this.dataset.getNumberofUsers();i++){
                int x=rand.nextInt((int)users.size())+1;
                for(long k : chosenRandomly){
                    while(k == x){
                        x = rand.nextInt((int)users.size())+1;
                    }
                }
                chosenRandomly[i]=x;
            }


            /*System.out.println(rand.nextInt((int)this.dataset.getNumberofUsers()));
            System.out.println(rand.nextInt((int)this.dataset.getNumberofUsers()));
            System.out.println(rand.nextInt((int)this.dataset.getNumberofUsers()));*/
            users.forEach(entry->{ //getting all the informations in loop

                JSONObject user = (JSONObject) entry;
                User eachUser = new User();
                long currentUserID=(long)user.get("user id");
                for(int i=0;i<chosenRandomly.length;i++){
                    if((long)currentUserID==chosenRandomly[i]){
                        eachUser.setUserID((long)user.get("user id"));
                        eachUser.setUsername((String)user.get("user name"));
                        eachUser.setUserType((String)user.get("user type"));
                        userList.add(eachUser);
                    }
                }
                

            });
            this.dataset.setUsers(userList);
        }
            catch(IOException ie){ // exception catching
                ie.printStackTrace();
            }
            catch(ParseException ie){
                ie.printStackTrace();
            }
    }

    public void readDataset(String fileName){
        List<Label> labelList = new ArrayList<Label>();
        List<Instance> instanceList = new ArrayList<Instance>();
        Dataset newDataset = new Dataset();
        JSONParser jsonParser = new JSONParser(); //create JSONparser

        try(FileReader reader = new FileReader(fileName)){ //try to read the file

            Object obj = jsonParser.parse(reader); // read that file

            JSONObject jsonObject = (JSONObject) obj; // create JSONobject to pull the data
            
            long databaseID = (long) jsonObject.get("dataset id");  //get, gets the corresponding data
            String datasetName = (String) jsonObject.get("dataset name");
            long maxLabels = (long) jsonObject.get("maximum number of labels per instance");
            JSONArray labels = (JSONArray) jsonObject.get("class labels");
            JSONArray instances = (JSONArray) jsonObject.get("instances");

            newDataset.setDatasetID(databaseID); // getting all the data and create corresponding Dataset object
            newDataset.setDatasetName(datasetName);
            newDataset.setMaximumLabels(maxLabels);


            labels.forEach(entry-> { //getting label because it is in JSONArray
                JSONObject label = (JSONObject) entry;
                Label eachLabel = new Label();
                eachLabel.setLabelID((long) label.get("label id"));
                eachLabel.setLabelText((String) label.get("label text"));
                labelList.add(eachLabel);
            });

            instances.forEach(entry->{ //getting isntances because it is in JSONArray
                JSONObject instance = (JSONObject) entry;
                Instance eachInstance = new Instance();
                eachInstance.setInstanceID((long) instance.get("id"));
                eachInstance.setInstanceText((String) instance.get("instance"));
                instanceList.add(eachInstance);
            });

            newDataset.setLabels(labelList); // setting label for that dataset
            newDataset.setInstances(instanceList); // setting instances for that dataset
            this.dataset = newDataset; 
            
        }

        catch(IOException ie){ // exception catching
            ie.printStackTrace();
        }
        catch(ParseException ie){
            ie.printStackTrace();
        }

        
    }

    public Dataset getDataset(){

        return this.dataset; //getter for Dataset

    }

    public void writeNewAssigneeds(String filename, List<AssignedLabel> assignedLabelList){
        JSONObject samplObject = new JSONObject();

        samplObject.put("dataset id", this.dataset.getDatasetID()); // put, puts corresponding set of data
        samplObject.put("dataset name", this.dataset.getDatasetName());
        samplObject.put("maximum number", this.dataset.getMaximumLabels());
        JSONArray classLabels = new JSONArray(); //creating JSONarray to fill in with labels because it is an array, we need for loop
        for(int i=0;i<this.dataset.getLabels().size();i++){
            JSONObject labelDetails = new JSONObject();
            labelDetails.put("label id", this.dataset.getLabels().get(i).getLabelID());
            labelDetails.put("label text", this.dataset.getLabels().get(i).getLabelText());
            classLabels.add(labelDetails);
        }

        samplObject.put("class labels", classLabels); //putting classlabels


        JSONArray classInstances = new JSONArray(); 
        for(int i=0;i<this.dataset.getInstances().size();i++){ //creating JSONarray to fill in with instances because it is an array, we need for loop
            JSONObject instanceDetails = new JSONObject();
            instanceDetails.put("id", this.dataset.getInstances().get(i).getInstanceID());
            instanceDetails.put("instance", this.dataset.getInstances().get(i).getInstanceText());
            classInstances.add(instanceDetails);
        }

        samplObject.put("instances", classInstances); //putting instances

        JSONArray users = new JSONArray();
        for(int i=0;i<this.dataset.getUsers().size();i++){ //looping through users to put to JSON
            JSONObject userDetails = new JSONObject();
            userDetails.put("user id", this.dataset.getUsers().get(i).getUserID());
            userDetails.put("user name", this.dataset.getUsers().get(i).getUsername());
            userDetails.put("user type", this.dataset.getUsers().get(i).getuserType());
            users.add(userDetails);
        }

        samplObject.put("users",users); //putting to jsonfile


        JSONArray assignments = new JSONArray();
        for(int i=0;i<assignedLabelList.size();i++){
            JSONObject assignedLabelDetails = new JSONObject();
            assignedLabelDetails.put("instance id", assignedLabelList.get(i).getInstance().getInstanceID());
            assignedLabelDetails.put("class label id",(assignedLabelList.get(i).getClassLabelID()[0].getLabelID()));
            assignedLabelDetails.put("user id", assignedLabelList.get(i).getUser().getUserID());
            assignedLabelDetails.put("datetime", assignedLabelList.get(i).getLocalTime());
            assignments.add(assignedLabelDetails);
        
        }
        System.out.println("done");
        samplObject.put("class label assignments", assignments);

        try{
            Files.write(Paths.get(filename), samplObject.toJSONString().getBytes());
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public void writeDatasetMetrics(AssignedLabel assignedLabel,Dataset currentDataset,DatasetPerformance datasetPerformance){
        JSONObject userMetricObject = new JSONObject(); // Top JSON object
        JSONArray userMetric = new JSONArray();  //JSON object to keep user metric metrics
        JSONObject userMetricDetails = new JSONObject();  
        
        userMetricObject.put("Number of users assigned to this dataset", currentDataset.getNumberofUsers());

        userMetricDetails.put("UserID", assignedLabel.getUser().getUserID());
        userMetricDetails.put("InstanceID",assignedLabel.getInstance().getInstanceID());
        userMetricDetails.put("ClassLabelID",(assignedLabel.getClassLabelID()[0].getLabelID()));
        userMetricDetails.put("Time", assignedLabel.getLocalTime());
        userMetric.add(userMetricDetails);
        
        
        userMetricObject.put(("Reports for Dataset "+ currentDataset.getDatasetID()), userMetric); 

        JSONArray datasetMetric = new JSONArray();
        JSONObject datasetMetricDetails = new JSONObject();
        datasetMetricDetails.put("Dataset "+currentDataset.getDatasetID() + " Percentage",datasetPerformance.getCompletenessPercentage());
        datasetMetric.add(datasetMetricDetails);

        userMetricObject.put("Repots", datasetMetric);


        try{
            File userMetricFile = new File ("./iteration2/DatasetPerformanceAndMetrics.json"); //open the file
            if(!userMetricFile.exists()) { //if file does not exits create a new one
                userMetricFile.createNewFile(); 
            }

            if(userMetricFile.exists() && !userMetricFile.isDirectory()) { //is exist append it
                FileWriter file = new FileWriter(userMetricFile,true);
                file.write("\n"+userMetricObject.toJSONString()+"\n");
                file.flush();
                file.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void writeUserMetrics(Dataset dataset, UserPerformance uPerformance){
        JSONObject userMetricObject = new JSONObject(); // Top JSON object
        JSONObject userObject=new JSONObject();
        JSONArray userMetric = new JSONArray();  //JSON object to keep user metric metrics
        JSONObject userMetricDetails = new JSONObject();  

        for (int i=0; i<dataset.getUsers().size(); i++){
            User cUser=dataset.getUsers().get(i);
            if (cUser.getUserID()==uPerformance.getCurrentUser().getUserID()){
                userMetricDetails.put("Number of datasets assigned", uPerformance.getNAssignedDatasets());
                userMetricDetails.put("List of all datasets with their completeness percentage", uPerformance.getNInstanceLabelled());
                userMetricDetails.put("Total number of instances labeled", uPerformance.getNInstanceLabelled());
                userMetricDetails.put("Total number of unique instances labeled ", uPerformance.getNUniqueInstancesLabelled());
                userMetricDetails.put("Consistency percentage", uPerformance.getConcistencyPercentage());
                userMetricDetails.put("Average time spent in labeling an instance in seconds", uPerformance.getAvgTimeSpent());
                userMetricDetails.put("Std. dev. of  time spent in labeling an instance in seconds", uPerformance.getStdTimeSpent()); 
                userMetric.add(userMetricDetails);
                userObject.put("User"+dataset.getUsers().get(i).getUserID(),userMetric); 
            }
           
        } 
        userMetricObject.put("User Performance Metrics and Reports", userObject);

        try{
            File userMetricFile = new File ("./iteration2/UserPerformanceMetric.json"); //open the file
            if(!userMetricFile.exists()) { //if file does not exits create a new one
                userMetricFile.createNewFile(); 
            }

            if(userMetricFile.exists() && !userMetricFile.isDirectory()) { //is exist append it
                FileWriter file = new FileWriter(userMetricFile,true);
                file.write("\n"+userMetricObject.toJSONString()+"\n");
                file.flush();
                file.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void sumInstanceMetrics(InstancePerformance iPerformance){
        this.nLabelAssignments+=iPerformance.getNLabelAssignments();
        this.nUniqueLabelAssignments+=iPerformance.getNUniqueLabelAssignments();
        this.nUniqueUsers+=iPerformance.getNUniqueUsers();

        writeInstanceMetrics(iPerformance, this.nLabelAssignments, this.nUniqueLabelAssignments, this.nUniqueUsers);
    }

//parametre değiştirilecek
    public void writeInstanceMetrics(InstancePerformance iPerformance,int nLabelAssignments,int nUniqueLabelAssignments,int nUniqueUsers){
        JSONObject instanceMetricObject = new JSONObject(); // Top JSON object
        JSONObject instanceObject=new JSONObject();
        JSONArray instanceMetric = new JSONArray();  //JSON object to keep user metric metrics
        JSONObject instanceMetricDetails = new JSONObject();  


        instanceMetricDetails.put("Total number of label assignments ", nLabelAssignments);
        instanceMetricDetails.put("Number of unique label assignments ", nUniqueLabelAssignments);
        instanceMetricDetails.put("Number of unique users ", nUniqueUsers);
        instanceMetricDetails.put("Most frequent class label and percentage ", iPerformance.getMostFrequentClassLabel());
        instanceMetricDetails.put("List class labels and percentages ", iPerformance.getClassLabels());
        instanceMetricDetails.put("Entropy ", iPerformance.getEntropy()); 
        instanceMetric.add(instanceMetricDetails);
    
        instanceMetricObject.put("Instance Performance Metrics and Reports", instanceMetric);

        try{
            File userMetricFile = new File ("./iteration2/InstancePerformanceMetric.json"); //open the file
            if(!userMetricFile.exists()) { //if file does not exits create a new one
                userMetricFile.createNewFile(); 
            }

            if(userMetricFile.exists() && !userMetricFile.isDirectory()) { //is exist append it
                FileWriter file = new FileWriter(userMetricFile,true);
                file.write("\n"+instanceMetricObject.toJSONString()+"\n");
                file.flush();
                file.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


}
