package com.umg.ecotrack.dao.impl;

import com.umg.ecotrack.dao.PointDao;
import com.umg.ecotrack.db.DB;
import com.umg.ecotrack.model.Point;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PointDaoImpl implements PointDao {

    private Point map(ResultSet rs) throws SQLException {
        BigDecimal latBD = rs.getBigDecimal("latitude");
        BigDecimal lonBD = rs.getBigDecimal("longitude");
        Double lat = (latBD == null) ? null : latBD.doubleValue();
        Double lon = (lonBD == null) ? null : lonBD.doubleValue();

        return new Point(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("address"),
            lat,
            lon,
            rs.getString("manager")
        );
    }

    @Override
    public List<Point> list(String nameLike) {
        List<Point> out = new ArrayList<>();
        String sql = "SELECT * FROM points WHERE name LIKE ? ORDER BY name";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + (nameLike == null ? "" : nameLike) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error SQL (listar puntos): " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        return out;
    }

    @Override
    public Point findById(int id) {
        String sql = "SELECT * FROM points WHERE id=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error SQL (buscar punto): " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    @Override
    public boolean insert(Point p) {
        String sql = "INSERT INTO points(name,address,latitude,longitude,manager) VALUES(?,?,?,?,?)";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getAddress());
            if (p.getLatitude()==null) ps.setNull(3, Types.NUMERIC);
            else ps.setBigDecimal(3, BigDecimal.valueOf(p.getLatitude()));
            if (p.getLongitude()==null) ps.setNull(4, Types.NUMERIC);
            else ps.setBigDecimal(4, BigDecimal.valueOf(p.getLongitude()));
            ps.setString(5, p.getManager());

            return ps.executeUpdate()==1;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error SQL (insertar punto): " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public boolean update(Point p) {
        String sql = "UPDATE points SET name=?, address=?, latitude=?, longitude=?, manager=? WHERE id=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getAddress());
            if (p.getLatitude()==null) ps.setNull(3, Types.NUMERIC);
            else ps.setBigDecimal(3, BigDecimal.valueOf(p.getLatitude()));
            if (p.getLongitude()==null) ps.setNull(4, Types.NUMERIC);
            else ps.setBigDecimal(4, BigDecimal.valueOf(p.getLongitude()));
            ps.setString(5, p.getManager());
            ps.setInt(6, p.getId());
            return ps.executeUpdate()==1;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error SQL (actualizar punto): " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
public boolean delete(int id) {
    String countSql = "SELECT COUNT(1) FROM collections WHERE point_id=?";
    String delSql   = "DELETE FROM points WHERE id=?";

    try (Connection cn = DB.get()) {
        // 1) ¿Tiene recolecciones asociadas?
        try (PreparedStatement ps = cn.prepareStatement(countSql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                        "No se puede eliminar: el punto tiene recolecciones asociadas.\n" +
                        "Primero elimina/mueve esas recolecciones.");
                    return false;
                }
            }
        }
        // 2) Borrar si no tiene dependencias
        try (PreparedStatement ps = cn.prepareStatement(delSql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
        javax.swing.JOptionPane.showMessageDialog(null,
            "Error SQL (eliminar punto): " + e.getMessage());
        return false;
    }
}

}
