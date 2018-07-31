package com.ora.blockchain.service.block.impl;

import com.ora.blockchain.mybatis.entity.block.Block;
import com.ora.blockchain.mybatis.entity.input.Input;
import com.ora.blockchain.mybatis.entity.output.Output;
import com.ora.blockchain.mybatis.entity.transaction.Transaction;
import com.ora.blockchain.mybatis.mapper.block.BlockMapper;
import com.ora.blockchain.mybatis.mapper.input.InputMapper;
import com.ora.blockchain.mybatis.mapper.output.OutputMapper;
import com.ora.blockchain.mybatis.mapper.transaction.TransactionMapper;
import com.ora.blockchain.service.block.IBlockService;
import com.ora.blockchain.service.rpc.IRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public abstract class BlockServiceImpl implements IBlockService {
    @Autowired
    private BlockMapper blockMapper;
    @Autowired
    private TransactionMapper transMapper;
    @Autowired
    private InputMapper inputMapper;
    @Autowired
    private OutputMapper outputMapper;

    @Override
    @Transactional
    public void insertBlock(String database, Block block) {
        blockMapper.insertBlock(database, block);
        List<Transaction> transactionList = getRpcService().getTransactionList(1, block.getBlockHash());
        if (null != transactionList && !transactionList.isEmpty()) {
            List<Output> outputList = new ArrayList<>();
            List<Input> inputList = new ArrayList<>();
            for (Transaction t : transactionList) {
                outputList.addAll(t.getOutputList());
                inputList.addAll(t.getInputList());
            }
            transMapper.insertTransactionList(database, transactionList);
            inputMapper.insertInputList(database, inputList);
            outputMapper.insertOutputList(database, outputList);
//            for (Input input : inputList) {
//                outputMapper.updateOutput(database, Output.STATUS_SPENT, input.getTransactionTxid(), input.getVout());
//            }
        }
    }

    @Override
    public void updateBlock(String database, List<Block> paramList) {
        if (null == paramList || paramList.isEmpty()) {
            return;
        }
        List<Block> blockList = blockMapper.queryBlockList(database, null, paramList.size());
        updateBlock(database, blockList, paramList);
    }

    @Override
    public void updateBlock(String database, List<Block> dbList, List<Block> paramList) {
        List<Block> dbBlockList = new ArrayList<>();
        if (null != dbList) {
            dbBlockList = dbList.stream().sorted(Comparator.comparing(Block::getHeight)).collect(Collectors.toList());
        }
        List<Block> paramBlockList = new ArrayList<>();
        if (null != paramList) {
            paramBlockList = paramList.stream().sorted(Comparator.comparing(Block::getHeight)).collect(Collectors.toList());
        }
        int dbIter = 0;
        int paraIter = 0;
        int dbSize = dbBlockList.size();
        int paraSize = paramBlockList.size();

        while (dbIter < dbSize || paraIter < paraSize) {
            if (dbIter == dbSize) {
                insertBlock(database, paramBlockList.get(paraIter));
                paraIter += 1;
                continue;
            }
            if (paraIter == paraSize) {
                //deleteBlock()
                dbIter += 1;
                continue;
            }
            if (dbBlockList.get(dbIter).getHeight().longValue() == paramBlockList.get(paraIter).getHeight().longValue()) {
                //更新当前区块所有交易
                updateBlock(database, dbBlockList.get(dbIter),paramBlockList.get(paraIter));
                dbIter += 1;
                paraIter += 1;
            } else if (dbBlockList.get(dbIter).getHeight().longValue() < paramBlockList.get(paraIter).getHeight().longValue()) {
                insertBlock(database, paramBlockList.get(paraIter));
                paraIter += 1;
            } else {
                //deleteblock()
                dbIter += 1;
            }
        }
    }

    @Override
    @Transactional
    public void updateBlock(String database, Block dbBlock,Block paramBlock) {
        if (null == paramBlock) {
            return;
        }
        //如果当前区块高等且BlockHash不相等，删除数据库的区块并写入链上区块
        if(!dbBlock.getBlockHash().equals(paramBlock.getBlockHash())){
            blockMapper.deleteBlockByBlockHash(database,dbBlock.getBlockHash());
            outputMapper.deleteOutput(database,dbBlock.getBlockHash());
            inputMapper.deleteInput(database,dbBlock.getBlockHash());
        }
        //当前区块包含的链上交易记录
        List<Transaction> paramList = getRpcService().getTransactionList(1, paramBlock.getBlockHash());
        if (null == paramList || paramList.isEmpty()) {
            return;
        }
        paramList = paramList.stream().sorted(Comparator.comparing(Transaction::getTxid)).collect(Collectors.toList());
        //当前区块包含的数据库中交易记录
        List<Transaction> dbList = transMapper.queryTransactionListByBlockHash(database, paramBlock.getBlockHash());
        if (null != dbList) {
            dbList = dbList.stream().sorted(Comparator.comparing(Transaction::getTxid)).collect(Collectors.toList());
        }
        int dbIter = 0;
        int paraIter = 0;
        int dbSize = dbList.size();
        int paraSize = paramList.size();

        while (dbIter < dbSize || paraIter < paraSize) {
            if (dbIter == dbSize) {
                transMapper.insertTransaction(database, paramList.get(paraIter));
                if (null != paramList.get(paraIter).getInputList() && !paramList.get(paraIter).getInputList().isEmpty()) {
                    inputMapper.insertInputList(database, paramList.get(paraIter).getInputList());
                }
                if (null != paramList.get(paraIter).getOutputList() && !paramList.get(paraIter).getOutputList().isEmpty()) {
                    outputMapper.insertOutputList(database, paramList.get(paraIter).getOutputList());
                }
                paraIter += 1;
                continue;
            }
            if (paraIter == paraSize) {
                //deleteTransaction()
                dbIter += 1;
                continue;
            }
            if (dbList.get(dbIter).getTxid().compareTo(paramList.get(paraIter).getTxid()) == 0) {
//                updateTransaction();
                dbIter += 1;
                paraIter += 1;
            } else if (dbList.get(dbIter).getTxid().compareTo(paramList.get(paraIter).getTxid()) > 0) {
                transMapper.insertTransaction(database, paramList.get(paraIter));
                if (null != paramList.get(paraIter).getInputList() && !paramList.get(paraIter).getInputList().isEmpty()) {
                    inputMapper.insertInputList(database, paramList.get(paraIter).getInputList());
                }
                if (null != paramList.get(paraIter).getOutputList() && !paramList.get(paraIter).getOutputList().isEmpty()) {
                    outputMapper.insertOutputList(database, paramList.get(paraIter).getOutputList());
                }
                paraIter += 1;
            } else {
                //deleteTransaction()
                dbIter += 1;
            }
        }
    }

    public List<Block> queryBlockList(String database, Long height, int size) {
        return blockMapper.queryBlockList(database, height, size);
    }

    public abstract IRpcService getRpcService();
}
