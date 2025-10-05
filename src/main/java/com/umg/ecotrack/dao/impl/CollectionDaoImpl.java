package com.umg.ecotrack.dao.impl;

import com.umg.ecotrack.dao.CollectionDao;
import com.umg.ecotrack.db.DB;
import com.umg.ecotrack.model.Collection;
import com.umg.ecotrack.model.ReportRow;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CollectionDaoImpl implements CollectionDao {

    // Mapear un row → objeto Collection
    private Collection map(ResultSet rs) throws SQLException {
        BigDecimal w = rs.getBigDecimal("weight_kg");
        return new Collection(
                rs.getInt("id"),
                rs.getInt("point_id"),
                rs.getDate("date").toLocalDate(),
                rs.getString("type"),
                (w == null ? 0.0 : w.doubleValue())
        );
    }

    @Override
    public List<Collection> list(LocalDate from, LocalDate to, Integer pointId) {
        List<Collection> out = new ArrayList<>();
        String sql = "SELECT * FROM collections WHERE date BETWEEN ? AND ?"
                   + (pointId != null ? " AND point_id=?" : "")
                   + " ORDER BY date DESC, id DESC";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            if (pointId != null) ps.setInt(3, pointId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error SQL (listar recolecciones): " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return out;
    }

    @Override
    public boolean insert(Collection c) {
        String sql = "INSERT INTO collections(point_id, date, type, weight_kg) VALUES(?,?,?,?)";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, c.getPointId());
            ps.setDate(2, Date.valueOf(c.getDate()));
            ps.setString(3, c.getType());
            ps.setBigDecimal(4, BigDecimal.valueOf(c.getWeightKg()));

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error SQL (insertar recolección): " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public boolean update(Collection c) {
        String sql = "UPDATE collections SET point_id=?, date=?, type=?, weight_kg=? WHERE id=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, c.getPointId());
            ps.setDate(2, Date.valueOf(c.getDate()));
            ps.setString(3, c.getType());
            ps.setBigDecimal(4, BigDecimal.valueOf(c.getWeightKg()));
            ps.setInt(5, c.getId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error SQL (actualizar recolección): " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM collections WHERE id=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error SQL (eliminar recolección): " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public List<ReportRow> monthlySumByType(LocalDate from, LocalDate to, Integer pointId) {
        List<ReportRow> rows = new ArrayList<>();

        String sql = """
            SELECT FORMAT(date,'yyyy-MM') AS mes, type, SUM(weight_kg) AS total_kg
            FROM collections
            WHERE date BETWEEN ? AND ?
        """;
        if (pointId != null) sql += " AND point_id=? ";
        sql += "GROUP BY FORMAT(date,'yyyy-MM'), type ORDER BY mes, type";

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            if (pointId != null) ps.setInt(3, pointId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total_kg");
                    rows.add(new ReportRow(
                            rs.getString("mes"),
                            rs.getString("type"),
                            total == null ? 0.0 : total.doubleValue()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error SQL (reporte mensual): " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return rows;
    }
}
