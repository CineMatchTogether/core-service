package com.service.core.security.services;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.tribuo.Example;
import org.tribuo.Feature;
import org.tribuo.MutableDataset;
import org.tribuo.common.tree.TreeModel;
import org.tribuo.data.csv.CSVLoader;
import org.tribuo.impl.ArrayExample;
import org.tribuo.regression.RegressionFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.rtree.CARTRegressionTrainer;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieRegressionService {
    private TreeModel<Regressor> model;
    private final Map<String, double[]> movieFeatures = new HashMap<>();
    private final String[] featureNames = {
            "year", "duration", "votes", "meta_score", "budget", "opening_weekend_gross", "gross_worldwide"
    };

    @PostConstruct
    public void init() {
        try {
            // Загрузка данных и обучение модели
            var regressionFactory = new RegressionFactory();
            var csvLoader = new CSVLoader<>(',', regressionFactory);
            var source = csvLoader.loadDataSource(Paths.get("data/movies_numeric.csv"), "rating");

            var dataset = new MutableDataset<>(source);
            var trainer = new CARTRegressionTrainer(15);
            model = trainer.train(dataset);

            // Загрузка признаков для поиска по названию
            try (CSVReader reader = new CSVReader(new FileReader("data/movies_numeric_with_titles.csv"))) {
                String[] header = reader.readNext(); // заголовок
                String[] row;
                while ((row = reader.readNext()) != null) {
                    String title = row[0].trim().replaceAll("\"", "");
                    double[] features = new double[featureNames.length];
                    for (int i = 0; i < featureNames.length; i++) {
                        features[i] = Double.parseDouble(row[i + 1]);
                    }
                    movieFeatures.put(title.toLowerCase(), features);
                }
            }
            System.out.println("Модель обучена и данные загружены.");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации модели", e);
        }
    }

    public Double predictRating(String title) {
        double[] input = movieFeatures.get(title.toLowerCase());
        if (input == null) return null;

        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < featureNames.length; i++) {
            features.add(new Feature(featureNames[i], input[i]));
        }

        Example<Regressor> example = new ArrayExample<>(new Regressor("DIM-0", 0), features);
        return (double) Math.round(model.predict(example).getOutput().getValues()[0] * 100) / 100;
    }
}
