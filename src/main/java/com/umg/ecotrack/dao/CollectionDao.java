package com.umg.ecotrack.dao;

import com.umg.ecotrack.model.Collection;
import com.umg.ecotrack.model.ReportRow;
import java.time.LocalDate;
import java.util.List;

public interface CollectionDao {
    List<Collection> list(LocalDate from, LocalDate to, Integer pointId);
    boolean insert(Collection c);
    boolean update(Collection c);
    boolean delete(int id);
    List<ReportRow> monthlySumByType(LocalDate from, LocalDate to, Integer pointId);
}
