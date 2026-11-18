package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.ClubMembershipDao;
import com.club.managementsystem.entity.ClubMembership;
import com.club.managementsystem.repository.ClubMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClubMembershipDaoImpl implements ClubMembershipDao {

    @Autowired
    private ClubMembershipRepository repository;

    @Override
    public ClubMembership save(ClubMembership membership) {
        return repository.save(membership);
    }

    @Override
    public Optional<ClubMembership> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<ClubMembership> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    @Override
    public List<ClubMembership> findByStudentId(String studentId) {
        return repository.findAll().stream()
                .filter(m -> studentId.equals(m.getStudentId()))
                .toList();
    }

    @Override
    public List<ClubMembership> findByClubId(Integer clubId) {
        return repository.findAll().stream()
                .filter(m -> clubId.equals(m.getClubId()))
                .toList();
    }

    @Override
    public List<ClubMembership> findByStatus(String status) {
        return repository.findAll().stream()
                .filter(m -> status.equals(m.getStatus()))
                .toList();
    }
}
