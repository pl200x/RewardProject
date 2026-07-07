package com.example.PrizeCenter.controller;

import com.example.PrizeCenter.controller.cmd.PrizeRecordCmd;
import com.example.PrizeCenter.controller.converter.PrizeRewardConverter;
import com.example.PrizeCenter.controller.vo.*;
import com.example.PrizeCenter.entity.PrizeRecord;
import com.example.PrizeCenter.service.PrizeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prize_reward")
public class PrizeRewardController {
    @Autowired
    private PrizeRecordService prizeRecordService;
    @GetMapping("/get_all")
    public MultiPagePrizeRecordVO queryAll(){
        long start = System.currentTimeMillis();
        MultiPagePrizeRecordVO multiPagePrizeRecordVO = new MultiPagePrizeRecordVO();
        try{
            List<PrizeRecord> prizeRecordList = prizeRecordService.queryAll();
            multiPagePrizeRecordVO.setPrizeRecordVOList(PrizeRewardConverter.convertToVOList(prizeRecordList));
            multiPagePrizeRecordVO.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        }catch(Exception e){
            multiPagePrizeRecordVO.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return multiPagePrizeRecordVO;
    }
    @PostMapping("/add")
    public BaseVO addPrizeRecord(@RequestBody PrizeRecordCmd cmd) {
        long start = System.currentTimeMillis();
        try {
            prizeRecordService.addPrizeRecord(cmd);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        } catch (Exception e) {
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error");
        }
    }
    @GetMapping("/id")
    public SinglePagePrizeRecordVO queryById(@RequestParam int id) {
        long start = System.currentTimeMillis();
        SinglePagePrizeRecordVO page = new SinglePagePrizeRecordVO();
        try {
            PrizeRecord record = prizeRecordService.queryById(id);
            PrizeRecordVO vo = PrizeRewardConverter.convertToVO(record);
            page.setPrizeRewardVO(vo);
            page.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            page.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return page;
    }
    @GetMapping("/out_biz")
    public SinglePagePrizeRecordVO queryByOutBizNo(@RequestParam String outbizno) {
        long start = System.currentTimeMillis();
        SinglePagePrizeRecordVO page = new SinglePagePrizeRecordVO();
        try {
            PrizeRecord record = prizeRecordService.queryByOutBizNo(outbizno);
            PrizeRecordVO vo = PrizeRewardConverter.convertToVO(record);
            page.setPrizeRewardVO(vo);
            page.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            page.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return page;
    }
    @DeleteMapping("/delete_by_id")
    public BaseVO delete(int id) {
        long start = System.currentTimeMillis();
        try {
            prizeRecordService.deletePrizeRecord(id);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        } catch (Exception e) {
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error");
        }
    }

    @GetMapping("/count")
    public PrizeRecordCountVO getCount(){
        long start = System.currentTimeMillis();
        PrizeRecordCountVO countVO = new PrizeRecordCountVO();
        try{
           int count = prizeRecordService.getCount();
           countVO.setCount(count);
           BaseVO baseVO = BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
           countVO.setBaseVO(baseVO);
        }catch(Exception e){
            BaseVO baseVO = BaseVO.buildBaseVO(false,500,System.currentTimeMillis()-start,"unknown error");
            countVO.setBaseVO(baseVO);
        }
        return countVO;
    }
    @GetMapping("/record-by-minutes")
    public PrizeRecordListVO getList(){
        long start = System.currentTimeMillis();
        PrizeRecordListVO listVO = new PrizeRecordListVO();
        try{
            List<PrizeRecord> prizeRecordList = prizeRecordService.queryLastMinute();
            listVO.setPrizeRecordList(prizeRecordList);
            BaseVO baseVO = BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
            listVO.setBaseVO(baseVO);
        }catch(Exception e){
            BaseVO baseVO = BaseVO.buildBaseVO(false,500,System.currentTimeMillis()-start,"unknown error");
            listVO.setBaseVO(baseVO);
        }
        return listVO;
    }
}
