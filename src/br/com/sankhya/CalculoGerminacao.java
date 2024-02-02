package br.com.sankhya;

import java.util.ArrayList;
import java.util.Collections;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class CalculoGerminacao implements EventoProgramavelJava {

	@Override
	/*
	 * @ejb.interface-method tview-type="remote"
	 * 
	 * @ejb.transaction type="Required"
	 */
	public void afterInsert(PersistenceEvent event) throws Exception {
		JapeSession.SessionHandle hnd = null;
		try {
			ArrayList<Integer> germibox = new ArrayList<>();

			DynamicVO leituraVO = (DynamicVO) event.getVo();

			germibox.add(leituraVO.asInt("GERMIBOX1"));
			germibox.add(leituraVO.asInt("GERMIBOX2"));
			germibox.add(leituraVO.asInt("GERMIBOX3"));
			germibox.add(leituraVO.asInt("GERMIBOX4"));

			Collections.sort(germibox);

			// TODO: Arredondar
			double media = (germibox.get(0) + germibox.get(1) + germibox.get(2) + Double.valueOf(germibox.get(3))) / 4;

			// TODO: PEGAR TOLERANCIA

			int tolerancia = 0;

			// Se a diferença do maior valor com o menor valor for menor que a tolerancia.
			if (germibox.get(3) - germibox.get(0) > tolerancia) {
				
				media = (germibox.get(1) + germibox.get(2) + Double.valueOf(germibox.get(3))) / 3;
				
				// Se a diferença do maior valor com o menor valor for menor que a tolerancia.
				if (germibox.get(3) - germibox.get(0) > tolerancia) {
					// TODO: Aviso mostrando que a tolerancia é maior de novo.
					return;
				}
				return;
			}
			
			// Insere no resultado a media.

			/*
			 * JapeWrapper leituraDAO = JapeFactory.dao("AD_TGFLEI"); DynamicVO leituraVO =
			 * leituraDAO.findByPK(event.getVo());
			 */

		} catch (Exception e) {

		} finally {
			JapeSession.close(hnd);
		}

	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {

	}

	@Override
	public void afterDelete(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeCommit(TransactionContext tranCtx) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub

	}

}
