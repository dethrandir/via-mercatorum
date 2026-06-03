package com.viamercatorum.player;

import java.util.*;

public class NewspaperSystem {
    private final List<Newspaper> newspapers = new ArrayList<>();

    public record Newspaper(String title, String date, List<Article> articles) {}

    public record Article(String headline, String body, String sourceVillageId, int severity) {}

    public void publishNewspaper(String title, String date, List<Article> articles) {
        newspapers.add(new Newspaper(title, date, articles));
        if (newspapers.size() > 20) {
            newspapers.remove(0);
        }
    }

    public void publishDisasterNews(String villageName, String disasterType, int severity) {
        publishNewspaper(
            "The Mercantile Gazette",
            "Day " + newspapers.size(),
            List.of(new Article(
                disasterType + " Strikes " + villageName,
                getDisasterDescription(disasterType, severity, villageName),
                villageName, severity
            ))
        );
    }

    public void publishTradeNews(String villageName, String goodName) {
        publishNewspaper(
            "The Mercantile Gazette",
            "Day " + newspapers.size(),
            List.of(new Article(
                "Trade Boom in " + villageName,
                villageName + " reports record " + goodName + " exports this season.",
                villageName, 2
            ))
        );
    }

    private String getDisasterDescription(String disasterType, int severity, String village) {
        return switch (disasterType.toLowerCase()) {
            case "drought" -> "Severe drought has hit " + village + ". Crop failure expected. Food prices likely to rise.";
            case "bandit_raid" -> "Bandits have raided " + village + ". Stocks are low, and the roads are unsafe.";
            case "plague" -> "A mysterious plague sweeps through " + village + ". Population declining.";
            case "bumper_harvest" -> village + " celebrates a bountiful harvest! Surplus goods available for trade.";
            default -> village + " reports unusual activity.";
        };
    }

    public List<Newspaper> getAllNewspapers() {
        return List.copyOf(newspapers);
    }
}
