package com.example.simpleweb.serv;

import com.example.simpleweb.dao.BDao;
import com.example.simpleweb.dao.TDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServC {

    private final BDao bDao;

    private final TDao tDao;

    public ServC(BDao bDao, TDao tDao) {
        this.bDao = bDao;
        this.tDao = tDao;
    }

    public String getC(){
        bDao.getA();
        tDao.getT();
        bDao.getA();
        return "C";
    }
}
