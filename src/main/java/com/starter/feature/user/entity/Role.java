package com.starter.feature.user.entity;

import com.starter.feature.user.model.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_roles_name",
                columnNames = {"name"}
        )
)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
}
