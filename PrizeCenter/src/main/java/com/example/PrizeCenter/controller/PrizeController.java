package com.example.PrizeCenter.controller;

import com.example.PrizeCenter.controller.cmd.PrizeCmd;
import com.example.PrizeCenter.controller.cmd.SendRewardCmd;
import com.example.PrizeCenter.controller.converter.PrizeConverter;
import com.example.PrizeCenter.controller.vo.BaseVO;
import com.example.PrizeCenter.controller.vo.MultiPagePrizeVO;
import com.example.PrizeCenter.controller.vo.PrizeVO;
import com.example.PrizeCenter.controller.vo.SinglePagePrizeVO;
import com.example.PrizeCenter.entity.Prize;
import com.example.PrizeCenter.exception.PrizeDoNotExistedException;
import com.example.PrizeCenter.exception.PrizeStorageShortageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import com.example.PrizeCenter.service.PrizeService;

import java.util.List;

@RestController
@RequestMapping("/prize")
public class PrizeController {
    @Autowired
    private PrizeService prizeService;
    private static final Logger logger = LoggerFactory.getLogger(PrizeController.class);

    @PostMapping("/add")
    public BaseVO addPrize(@RequestBody PrizeCmd prizeCmd){
        long start = System.currentTimeMillis();
        try {
            prizeService.addPrize(prizeCmd);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);

        } catch (Exception e) {
            logger.error(e.toString());
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error");
        }
    }

    @GetMapping("/search")
    public SinglePagePrizeVO queryByCode(String code){
        long start = System.currentTimeMillis();
        SinglePagePrizeVO singlePagePrizeVO = new SinglePagePrizeVO();
        try{
            Prize prize = prizeService.queryByCode(code);
            PrizeVO prizeVO = PrizeConverter.convertToVO(prize);
            singlePagePrizeVO.setPrizeVO(prizeVO);
            singlePagePrizeVO.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        }catch(Exception e){
            logger.error(e.toString());
            singlePagePrizeVO.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return singlePagePrizeVO;
    }

    @PutMapping("/update")
    public BaseVO updatePrizeData(int id, int storage, String name, String description){
        long start = System.currentTimeMillis();
        try{
            // void updatePrize(int id,int storage,String name, String description);
            prizeService.updatePrize(id, storage, name, description);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        }catch (PrizeDoNotExistedException e) {
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, e.getMessage());
        } catch(Exception e){
            logger.error(e.toString());
            return BaseVO.buildBaseVO(false,500,System.currentTimeMillis()-start,"unknown error");
        }
    }
    @PostMapping("/send_reward")
    public BaseVO sendReward(@RequestBody SendRewardCmd rewardCmd){
        long start = System.currentTimeMillis();
        try{
            // void updatePrize(int id,int storage,String name, String description)
            prizeService.updatePrizeStorage(rewardCmd.getCode(), rewardCmd.getAmount(),rewardCmd.getOutbizno());
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        }catch (PrizeDoNotExistedException e) {
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, e.getMessage());
        } catch(DuplicateKeyException e){
            //catch DuplicateKeyException因为我们update时不catch for idempotent key, 避免insert重复的outbizno，
            //但是因为这是一个exception，所以要catch并返回200，否则结果为500
            return BaseVO.buildBaseVO(true,200,System.currentTimeMillis()-start,"same outbizno");
        }catch(PrizeStorageShortageException e){
            logger.error(e.toString());
            return BaseVO.buildBaseVO(false,500,System.currentTimeMillis()-start,e.getMessage());
        } catch(Exception e){
            logger.error(e.toString());
            return BaseVO.buildBaseVO(false,500,System.currentTimeMillis()-start,"unknown error");
        }
    }
    @GetMapping("/getallprize")
    public MultiPagePrizeVO getAllPrizeType(){
        long start = System.currentTimeMillis();
        MultiPagePrizeVO multiPagePrizeVO = new MultiPagePrizeVO();
        try{
            List<Prize> prizeList = prizeService.queryAll();
            List<PrizeVO> prizeVOList = PrizeConverter.convertToVOList(prizeList);
            multiPagePrizeVO.setPrizeVOList(prizeVOList);
            multiPagePrizeVO.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        }catch(Exception e){
            logger.error(e.toString());
            multiPagePrizeVO.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return multiPagePrizeVO;
    }
    @DeleteMapping("/delete")
    public BaseVO deletePrize(int id){
        long start = System.currentTimeMillis();
        try{
            prizeService.deletePrize(id);
            return BaseVO.buildBaseVO(true,200,System.currentTimeMillis() - start, null);
        }catch(Exception e){
            logger.error(e.toString());
            return BaseVO.buildBaseVO(false,500,System.currentTimeMillis()-start,"unknown error");
        }
    }
}
