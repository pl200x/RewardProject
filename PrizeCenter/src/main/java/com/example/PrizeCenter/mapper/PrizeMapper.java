package com.example.PrizeCenter.mapper;

import com.example.PrizeCenter.entity.Prize;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrizeMapper {
    Prize queryByCode(String code);
    Prize queryByCodeForUpdate(String code);
    Prize queryById(int id);
    void addPrize(Prize prize);
    void updatePrize(int id,int storage,String name, String description);
    int decrementPrizeStorage(@Param("code") String code, @Param("amount") int amount);
    List<Prize> queryAll();
    void deletePrize(int id);


}
