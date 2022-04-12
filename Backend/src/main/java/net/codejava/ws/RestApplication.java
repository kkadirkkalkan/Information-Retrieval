package net.codejava.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.util.Arrays;

@SpringBootApplication
public class RestApplication {

	public static void main(String[] args) throws Exception {
		// parse the first command line argument as number of documentsm, if not specificied it's 1000
		int numDocs = 1000;
		if (args.length > 0) {
			numDocs = Integer.parseInt(args[0]);
		}
		System.out.println("Number of documents: " + numDocs);


		// Create k-means cluster
		kMeansClusterer clusterer = kMeansClusterer.getInstance();

		// Load data from 1000topics.csv
		// TopicId,Topic,Context

		String filePath = "src/main/resources/topics_clean.csv";
		// Read through each line of the file
		try {
			BufferedReader br = new BufferedReader(new java.io.FileReader(filePath));
			int documentCount = 0;
			String line;
			while ((line = br.readLine()) != null && documentCount < numDocs) {
	            // Split the line into an array of strings
				// CSV file is split on comma
				String[] values = line.split(",");
				clusterer.addDocument(values[1], values[2]);
				documentCount++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return; // Something fucked up
		}

		// Cluster the data
		clusterer.beginClustering();

		// For debug print out clusters
		for (int i = 0; i < clusterer.getClusterCenters().length; i++) {
			double[] clusterCenter = clusterer.getClusterCenters()[i];
			double s = Arrays.stream(clusterCenter).sum();
			System.out.println("Cluster " + i + ": " + s);
		}

		SpringApplication.run(RestApplication.class, args);
	}
}
