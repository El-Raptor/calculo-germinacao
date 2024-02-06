package br.com.sankhya;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

public class CalculoGerminacao implements EventoProgramavelJava {

	@Override
	/*
	 * @ejb.interface-method tview-type="remote"
	 * 
	 * @ejb.transaction type="Required"
	 */
	public void afterInsert(PersistenceEvent ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		try {
			ArrayList<Integer> germibox = new ArrayList<>();

			DynamicVO leituraVO = (DynamicVO) ctx.getVo();

			germibox.add(leituraVO.asInt("GERMIBOX1"));
			germibox.add(leituraVO.asInt("GERMIBOX2"));
			germibox.add(leituraVO.asInt("GERMIBOX3"));
			germibox.add(leituraVO.asInt("GERMIBOX4"));

			Collections.sort(germibox);

			// Calcula a m�dia
			double media = (germibox.get(0) + germibox.get(1) + germibox.get(2) + Double.valueOf(germibox.get(3))) / 4;
			int result = (int) Math.round(media);

			// Obt�m a toler�ncia.
			int tolerancia = getTolerancia(ctx, result);
			
			// TODO: Validar a quantidade de Germibox
			// TODO: Validar se os valores da Germibox est� entre 0 e 100
			// TODO: Pegar o CODCLC para a leitura correta
			

			// Se a diferen�a do maior valor com o menor valor for menor que a tolerancia.
			if (germibox.get(3) - germibox.get(0) > tolerancia) {

				// Calcula a m�dia.
				media = (germibox.get(1) + germibox.get(2) + Double.valueOf(germibox.get(3))) / 3;
				result = (int) Math.round(media);

				// Obt�m toler�ncia
				tolerancia = getTolerancia(ctx, result);

				// Se a diferen�a do maior valor com o menor valor for menor que a tolerancia.
				if (germibox.get(3) - germibox.get(1) > tolerancia) {
					// TODO: Aviso mostrando que a tolerancia � maior de novo.
					return;
				}
			}

			// Insere no resultado a media.
			JapeWrapper laudoDAO = JapeFactory.dao(DynamicEntityNames.ITEM_LAUDO);
			laudoDAO.prepareToUpdateByPK(leituraVO.asBigDecimal("NUCLL"), new BigDecimal(7) /* CODCLC */)
					.set("RESULTADO", new BigDecimal(result))
					.update();
			//DynamicVO laudoVO = laudoDAO.findByPK(leituraVO.asBigDecimal("NUCLL"), new BigDecimal(7));

			/*if (result != 0)
				throw new Exception("NUCLL " + laudoVO.asBigDecimal("NUCLL"));*/

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}

	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {
		// TODO: Refazer o c�lculo se alterar.
	}
	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		// TODO: N�o pode alterar se estiver aprovado
	}

	@Override
	public void afterDelete(PersistenceEvent event) throws Exception {
	}

	@Override
	public void beforeCommit(TransactionContext tranCtx) throws Exception {}

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
	}

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {}


	/**
	 * Obt�m a toler�ncia do valor de germina��o.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	private int getTolerancia(PersistenceEvent ctx, int media) throws Exception {

		JapeWrapper germiDAO = JapeFactory.dao("AD_TGFGER");
		DynamicVO germiVO = (DynamicVO) germiDAO.findOne("RESULT = " + media);

		return germiVO.asInt("TOLERANCIA");
	}

}
