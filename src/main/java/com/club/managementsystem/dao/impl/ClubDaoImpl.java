package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.ClubDao;
import com.club.managementsystem.entity.Club;
import com.club.managementsystem.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Club DAO实现类 - 实现社团数据访问逻辑
 */
@Repository
public class ClubDaoImpl implements ClubDao {

    @Autowired
    private ClubRepository clubRepository;

    @Override
    public Club save(Club club) {
        return clubRepository.save(club);
    }

    @Override
    public Optional<Club> findById(Integer id) {
        return clubRepository.findById(id);
    }

    @Override
    public List<Club> findAll() {
        return clubRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        clubRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return clubRepository.existsById(id);
    }

    @Override
    public Optional<Club> findByName(String name) {
        return clubRepository.findAll().stream()
                .filter(c -> c.getClubName().equals(name))
                .findFirst();
    }

    @Override
    public List<Club> findByStatus(String status) {
        return clubRepository.findAll().stream()
                .filter(c -> status.equals(c.getStatus()))
                .toList();
    }
}
