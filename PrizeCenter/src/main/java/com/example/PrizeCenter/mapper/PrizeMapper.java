package com.example.PrizeCenter.mapper;

import com.example.PrizeCenter.entity.Prize;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PrizeMapper {
    Prize queryByCode(String code);
    Prize queryById(int id);
    void addPrize(Prize prize);
    void updatePrize(int id,int storage,String name, String description);
    void updatePrizeStorage(String code, int storage);
    List<Prize> queryAll();
    void deletePrize(int id);


}
