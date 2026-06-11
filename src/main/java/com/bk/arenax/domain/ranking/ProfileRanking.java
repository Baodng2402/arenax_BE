package com.bk.arenax.domain.ranking;

import com.bk.arenax.domain.common.BaseEntity;
import com.bk.arenax.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity(name = "profile_ranking")

public class ProfileRanking extends BaseEntity  {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    Integer eloZone;

    @Column(nullable = false)
    Integer eloGlobal;

    @Column(nullable = false)
    Integer winsMatch;

    @Column(nullable = false)
    Integer lossesMatch;

    @Column(nullable = false)
    String rank;


    public String getRanking() {
        return rank;
    }

    public Integer getEloZone() {return eloZone;}
    public Integer getEloGlobal() {return eloGlobal;}

    public Integer getMatches (String findType) {
        if(GameResult.WINS.toString().equalsIgnoreCase(findType)){
            return winsMatch;
        }
        else if(GameResult.LOSSES.toString().equalsIgnoreCase(findType)){
            return lossesMatch;
        }
        return winsMatch + lossesMatch;
    }
}
