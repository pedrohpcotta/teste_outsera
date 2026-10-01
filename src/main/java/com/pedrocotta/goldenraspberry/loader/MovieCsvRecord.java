package com.pedrocotta.goldenraspberry.loader;

import java.util.List;

public record MovieCsvRecord(int year, String title, List<String> studios, List<String> producers, boolean winner) {
}
