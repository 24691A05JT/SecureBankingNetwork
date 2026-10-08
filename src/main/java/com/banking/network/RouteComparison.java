package com.banking.network;

import java.util.Objects;

/**
 * Helper utility to compare routing results between Dijkstra Link-State and Distance Vector algorithms.
 */
public final class RouteComparison {

    private RouteComparison() {
        // Utility class
    }

    /**
     * Formats a side-by-side comparative table between Dijkstra and Distance Vector routing results.
     *
     * @param dijkstraResult Dijkstra algorithm result
     * @param dvResult Distance Vector algorithm result
     * @return formatted comparison table string
     */
    public static String formatComparison(DijkstraResult dijkstraResult, DistanceVectorResult dvResult) {
        Objects.requireNonNull(dijkstraResult, "DijkstraResult cannot be null");
        Objects.requireNonNull(dvResult, "DistanceVectorResult cannot be null");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("----------------------------------------------------------------------------------%n"));
        sb.append(String.format("ROUTING ALGORITHM COMPARISON%n"));
        sb.append(String.format("----------------------------------------------------------------------------------%n"));
        sb.append(String.format("%-18s | %-8s | %-18s | %s%n", "Algorithm", "Cost", "Next Hop", "Path"));
        sb.append(String.format("----------------------------------------------------------------------------------%n"));

        String dNextHop = dijkstraResult.getNextHop() != null ? dijkstraResult.getNextHop().getNodeName() : "LOCAL/NONE";
        String dvNextHop = dvResult.getNextHop() != null ? dvResult.getNextHop().getNodeName() : "LOCAL/NONE";

        sb.append(String.format("%-18s | %-8.1f | %-18s | %s%n",
                "Dijkstra", dijkstraResult.getTotalCost(), dNextHop, dijkstraResult.getFormattedPath()));

        sb.append(String.format("%-18s | %-8.1f | %-18s | %s%n",
                "Distance Vector", dvResult.getTotalCost(), dvNextHop, dvResult.getFormattedPath()));

        sb.append(String.format("----------------------------------------------------------------------------------%n"));

        boolean costsMatch = Math.abs(dijkstraResult.getTotalCost() - dvResult.getTotalCost()) < 1e-6;
        if (costsMatch && dijkstraResult.isReachable() && dvResult.isReachable()) {
            sb.append("Both routing algorithms successfully calculated the shortest available route.");
        } else if (!dijkstraResult.isReachable() && !dvResult.isReachable()) {
            sb.append("Both routing algorithms correctly identified the destination as UNREACHABLE.");
        } else {
            sb.append("Note: Algorithm results differ due to topology convergence or equal-cost multi-path selection.");
        }

        return sb.toString();
    }
}
