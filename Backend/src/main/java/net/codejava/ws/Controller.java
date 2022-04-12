package net.codejava.ws;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

@CrossOrigin(origins = "*")
@RestController
public class Controller {

    private static final int n = 10; // Number of nearest neighbors to return

    @RequestMapping("/search")
    public SearchResult Search(@RequestParam("query") String query) throws Exception {
        System.out.println("Query: " + query);
        kMeansClusterer clusterer = kMeansClusterer.getInstance();

        double queryVector[] = clusterer.getTfIdfVector(query);

        System.out.println("Query Vector: " + Arrays.stream(queryVector).sum());
        // Get 10 nearest document indices
        int[] nearest = clusterer.getNearestNeighbors(queryVector, n);
        System.out.println("Nearest: " + Arrays.toString(nearest));

        DocumentInformation[] documents = new DocumentInformation[nearest.length];
        for (int i = 0; i < nearest.length; i++) {
            int idx = nearest[i];
            double[] v = clusterer.getDocumentVectors()[idx];
            documents[i] = new DocumentInformation(
                    clusterer.getDocumentNames().get(idx),
                    clusterer.getDOCUMENTS().get(idx),
                    clusterer.getClassification(v),
                    v
            );
        }

        QueryInformation queryInfo = new QueryInformation(query, queryVector);

        return new SearchResult(queryInfo, documents);

    }
}

class SearchResult {
    public QueryInformation query;
    public DocumentInformation[] documents;

    public SearchResult(QueryInformation query, DocumentInformation[] documents) {
        this.query = query;
        this.documents = documents;
    }
}

// Data class for our query
class QueryInformation {
    public String query;
    public double[] vector;

    public QueryInformation(String query, double[] vector) {
        this.query = query;
        this.vector = vector;
    }
}

// Data class for a document
class DocumentInformation {
    public String title;
    public String context;
    public int classification;
    public double[] vector;

    public DocumentInformation(String title, String context, int classification, double[] vector) {
        this.title = title;
        this.context = context;
        this.classification = classification;
        this.vector = vector;
    }
}