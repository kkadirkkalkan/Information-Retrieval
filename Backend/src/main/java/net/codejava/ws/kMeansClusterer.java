package net.codejava.ws;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class kMeansClusterer {

    private static int k = 32;

    // Singleton instance
    private static kMeansClusterer instance = null;

    private final List<String> DocumentNames = new ArrayList<>();
    private final List<String> DOCUMENTS = new ArrayList<>();
    private final List<String> Dictionary = new ArrayList<>();
    private double[][] DocumentVectors;
    private double[][] ClusterCenters;

    private String[][] DocumentTokens;

    private boolean lockDocuments = false;

    // Singleton constructor
    private kMeansClusterer() {}

    // Add method to aadd documents to our clusterer
    public void addDocument(String documentName, String document) {
        if (lockDocuments) { return; }

        System.out.println("Adding document: " + documentName);

        this.DocumentNames.add(documentName);
        this.DOCUMENTS.add(document);
    }

    public void beginClustering() throws Exception {
        lockDocuments = true;
        System.out.println("Beginning clustering...");

        // Tokenize all documents
        Analyzer analyzer = getAnalyzer();

        System.out.println("Tokenized documents...");
        this.DocumentTokens = new String[DOCUMENTS.size()][];
        for (int i = 0; i < DOCUMENTS.size(); i++) {
            this.DocumentTokens[i] = getTokens(DOCUMENTS.get(i), analyzer);
        }

        // Add tokens to dictionary
        for (String[] documentTokens : this.DocumentTokens) {
            for (String token : documentTokens) {
                if (!this.Dictionary.contains(token)) {
                    this.Dictionary.add(token);
                }
            }
        }

        System.out.println("Dictionary size: " + this.Dictionary.size());
        System.out.println("Getting tf-idf Vectors for documents...");

        // Create a vector for each document
        this.DocumentVectors = new double[DOCUMENTS.size()][];
        for (int i = 0; i < DOCUMENTS.size(); i++) {
            // The nth entry in the vector is the tf-idf of the nth token in the dictionary
            this.DocumentVectors[i] = getTfIdfVector(DOCUMENTS.get(i));
            // Every 1000 documents, print out the progress
            if ((i+1) % 100 == 0) {
                System.out.println("Processed " + (i+1) + " documents...");
            }
        }

        // tf idf values for documents done, begin clustering
        // Begin k-means clustering

        // Initialize cluster centers
        this.ClusterCenters = new double[k][];
        for (int i = 0; i < k; i++) {
            this.ClusterCenters[i] = new double[this.Dictionary.size()];
            // Choose random document
            this.ClusterCenters[i] = this.DocumentVectors[(int) (Math.random() * this.DocumentVectors.length)];
        }

        // Iterate until convergence or max iterations
        System.out.println("Beginning clustering...");
        int iterations = 0;
        int maxIterations = 100;
        double[][] oldClusterCenters = new double[k][];
        while (iterations < maxIterations) {
            // Get distances of each document to each cluster
            double[][] distances = new double[k][];
            for (int i = 0; i < k; i++) {
                // For each cluster
                distances[i] = new double[DOCUMENTS.size()];
                for (int j = 0; j < DOCUMENTS.size(); j++) {
                    // For each document
                    double[] documentVector = this.DocumentVectors[j];
                    double[] clusterCenter = this.ClusterCenters[i];

                    // Distance is euclidean distance
                    double distance = euclideanDistance(documentVector, clusterCenter);
                    distances[i][j] = distance;
                }
            }

            // Assign each document to the closest cluster
            int[] assignments = new int[DOCUMENTS.size()];
            for (int i = 0; i < DOCUMENTS.size(); i++) {
                double minDistance = Double.MAX_VALUE;
                int minDistanceIndex = -1;
                for (int j = 0; j < k; j++) {
                    if (distances[j][i] < minDistance) {
                        minDistance = distances[j][i];
                        minDistanceIndex = j;
                    }
                }
                assignments[i] = minDistanceIndex;
            }

            // Update cluster centers
            oldClusterCenters = this.ClusterCenters;
            this.ClusterCenters = new double[k][];
            for (int i = 0; i < k; i++) {
                this.ClusterCenters[i] = new double[this.Dictionary.size()];
                List<double[]> clusterDocuments = new ArrayList<double[]>();
                for (int j = 0; j < DOCUMENTS.size(); j++) {
                    if (assignments[j] == i) {
                        clusterDocuments.add(this.DocumentVectors[j]);
                    }
                }

                // Average the document vectors
                this.ClusterCenters[i] = averageVectors(clusterDocuments);
            }
            // All cluster centers updated, check for convergence

            // Get sum of squared distances between old and new cluster centers
            double sumSquaredDistances = 0.0;
            for (int i = 0; i < k; i++) {
                double[] oldCenter = oldClusterCenters[i];
                double[] newCenter = this.ClusterCenters[i];

                // Sum of squared distances between old and new cluster centers
                double sumSquaredDistance = euclideanDistance(oldCenter, newCenter);
                sumSquaredDistances += sumSquaredDistance;
            }

            if (sumSquaredDistances < 0.001) {
                // Converged
                break;
            }
            iterations++;
            System.out.println("Iteration " + iterations + ": " + sumSquaredDistances);
        }

        // Clustering done
    }

    public int[] getNearestNeighbors(double[] documentVector, int n) {
        // Forgive me for I have sinned
        // Get distances of each document to our documentVector
        double[] distances = new double[DOCUMENTS.size()];
        for (int i = 0; i < DOCUMENTS.size(); i++) {
            // For each document
            double[] document = this.DocumentVectors[i];
            distances[i] = cosineSimilarity(document, documentVector);
        }
        // Print out the distances to two decimal places
        for (int i = 0; i < DOCUMENTS.size(); i++) {
            System.out.print(String.format("%.2f, ", distances[i]));
        }

        // Sort the distances preserving the original indices
        DoubleSortingElement[] sortedDistances = new DoubleSortingElement[distances.length];
        for (int i = 0; i < distances.length; i++) {
            sortedDistances[i] = new DoubleSortingElement(i, -distances[i]);
        }

        Arrays.sort(sortedDistances);
        // Get the indices of the top documents
        List<Integer> topIndices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // While the cosine similarity isn't 0
            if (sortedDistances[i].value == 0) {
                break;
            }
            topIndices.add(sortedDistances[i].index);
        }

        // Convert to array
        int[] topIndicesArray = new int[topIndices.size()];
        for (int i = 0; i < topIndices.size(); i++) {
            topIndicesArray[i] = topIndices.get(i);
        }

        return topIndicesArray;
    }

    /**
     * Gets the n nearest neighbor documents for a given document
     * @param document
     * @param n
     * @return List of indices of the n nearest neighbor documents
     * @throws IOException
     */
    public int[] getNearestNeighbors(String document, int n) throws IOException {
        // Turn document into tf-idf vector
        double[] documentVector = getTfIdfVector(document);

        // Get nearest neighbors
        return getNearestNeighbors(documentVector, n);
    }

    public double[] getTfIdfVector(String document) throws IOException {
        String[] tokens = getTokens(document, getAnalyzer());
        double[] tfidfVector = new double[this.Dictionary.size()];
        // For each unique word in the document
        for (String t: Arrays.stream(tokens).distinct().toArray(String[]::new)) {
            // Get the index of the word in the dictionary
            int index = this.Dictionary.indexOf(t);
            if (index == -1) {
                // Word not in dictionary
                continue;
            }
            // Count how often the word occurs in the document
            int documentFreq = 0;
            for (String documentToken : tokens) {
                if (documentToken.equals(t)) {
                    documentFreq++;
                }
            }

            // Count how often the word occurs in the corpus
            int totalFreq = 0;
            for (String[] documentTokens : this.DocumentTokens) {
                for (String documentToken : documentTokens) {
                    if (documentToken.equals(t)) {
                        totalFreq++;
                    }
                }
            }

            // Do some funny math to get tf-idf
            double tf = (double) documentFreq;
            double idf = Math.log10((double) DOCUMENTS.size() / (double) totalFreq);
            tfidfVector[index] = tf * idf;
        }

        return tfidfVector;
    }

    public int getClassification(double[] documentVector) {
        // Find nearest cluster center
        double minDistance = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < this.ClusterCenters.length; i++) {
            double distance = euclideanDistance(this.ClusterCenters[i], documentVector);
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }

        return minIndex;
    }

    /* =================================================================== */

    public List<String> getDocumentNames() {
        return DocumentNames;
    }

    public List<String> getDOCUMENTS() {
        return DOCUMENTS;
    }

    public List<String> getDictionary() {
        return Dictionary;
    }

    public double[][] getDocumentVectors() {
        return DocumentVectors;
    }

    public double[][] getClusterCenters() {
        return ClusterCenters;
    }

    public String[][] getDocumentTokens() {
        return DocumentTokens;
    }

    public boolean isLockDocuments() {
        return lockDocuments;
    }
    /* =================================================================== */

    // Get singleton instance
    public static kMeansClusterer getInstance() {
        if (instance == null) {
            instance = new kMeansClusterer();
        }
        return instance;
    }

    // static methods used by Lucene
    public static Analyzer getAnalyzer() throws IOException {
        return CustomAnalyzer.builder()
            .withTokenizer(StandardTokenizerFactory.class)
            .addTokenFilter(LowerCaseFilterFactory.class)
            .addTokenFilter(PorterStemFilterFactory.class)
            .build();
    }

    public static String[] getTokens(String document, Analyzer analyzer) throws IOException {
        TokenStream stream = analyzer.tokenStream("", document);
        stream.reset();
        List<String> tokens = new ArrayList<>();
        CharTermAttribute attribute = stream.addAttribute(CharTermAttribute.class);
        while (stream.incrementToken()) {
            tokens.add(attribute.toString());
        }
        stream.close();
        return tokens.toArray(new String[0]);
    }

    public static double euclideanDistance(double[] a, double[] b) {
        if (a.length != b.length) {
            //throw new IllegalArgumentException("Vectors must be the same length");
            return Double.MAX_VALUE;
        }

        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    // cosine similarity
    public static double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) {
            //throw new IllegalArgumentException("Vectors must be the same length");
            return Double.MAX_VALUE;
        }

        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static double[] averageVectors(List<double[]> vectors) {
        // Assuming all vectors are of the same length
        if (vectors.size() == 0) {
            return new double[0]; // Return empty vector
        }

        double[] mean = new double[vectors.get(0).length];
        for (int i = 0; i < mean.length; i++) {
            double sum = 0;
            for (double[] vector : vectors) {
                sum += vector[i];
            }
            mean[i] = sum / vectors.size();
        }
        return mean;
    }

}

class SortingElement<T extends Comparable<? super T>> implements Comparable<SortingElement<T>> {
    int index;
    T value;

    public SortingElement(int index, T value) {
        this.index = index;
        this.value = value;
    }

    public int compareTo(SortingElement<T> o) {
        return value.compareTo(o.value);
    }
}
class DoubleSortingElement extends SortingElement<Double> {
    public DoubleSortingElement(int index, Double value) {
        super(index, value);
    }
}