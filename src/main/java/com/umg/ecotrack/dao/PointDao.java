package com.umg.ecotrack.dao;

import com.umg.ecotrack.model.Point;
import java.util.List;

public interface PointDao {
    List<Point> list(String nameLike);
    Point findById(int id);
    boolean insert(Point p);
    boolean update(Point p);
    boolean delete(int id);
}
