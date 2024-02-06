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

/**
 * Esse programa realiza o c�lculo de germina��o das 
 * sementes por leitura realizada.
 * 
 * @author Felipe S. Lopes (felipe.lopes@sankhya.com.br)
 * @since 2023-10-03
 * 
 */
public class CalculoGerminacao implements EventoProgramavelJava {

	@Override
	/*
	 * @ejb.interface-method tview-type="remote"
	 * 
	 * @ejb.transaction type="Required"
	 */
	public void afterInsert(PersistenceEvent ctx) throws Exception {
		event(ctx);

	}

	@Override
	public void afterUpdate(PersistenceEvent ctx) throws Exception {
		event(ctx);
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
	 * Evento que realiza o c�lculo e o insere no resultado do laudo.
	 * @param ctx
	 * @throws MGEModelException
	 */
	private void event(PersistenceEvent ctx) throws MGEModelException {
		JapeSession.SessionHandle hnd = null;
		try {
			ArrayList<Integer> germibox = new ArrayList<>();
			int result = 0;

			DynamicVO leituraVO = (DynamicVO) ctx.getVo();

			germibox.add(leituraVO.asInt("GERMIBOX1"));
			germibox.add(leituraVO.asInt("GERMIBOX2"));
			germibox.add(leituraVO.asInt("GERMIBOX3"));
			germibox.add(leituraVO.asInt("GERMIBOX4"));

			Collections.sort(germibox);
			
			// Valida se o valor � acima de 100 
			if (germibox.removeIf(n -> (n > 100)))
				throw new Exception("N�o � permitido um valor acima de 100.");
			
			// Valida se o valor � abaixo de 0
			if (germibox.removeIf(n -> (n < 0)))
				throw new Exception("N�o � permitido um valor abaixo de 0.");
			
			// Remove germibox com o valor 0
			germibox.removeIf(n -> (n == 0));

			if (germibox.size() == 4) 
				result = calculaGermi(ctx, germibox);
			else if (germibox.size() == 3) 
				result = calculaGermi3(ctx, germibox);
			else 
				throw new Exception("N�mero de Germibox incorreto. Favor informar pelo menos 3 leituras.");
				
			
			// Insere no resultado a media.
			JapeWrapper laudoDAO = JapeFactory.dao(DynamicEntityNames.ITEM_LAUDO);
			laudoDAO.prepareToUpdateByPK(leituraVO.asBigDecimal("NUCLL"), leituraVO.asBigDecimal("CODCLC"))
					.set("RESULTADO", new BigDecimal(result))
					.update();
			//DynamicVO laudoVO = laudoDAO.findByPK(leituraVO.asBigDecimal("NUCLL"), new BigDecimal(7));


		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}
	}
	
	
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
	
	/**
	 * Realiza o c�lculo de germina��o.
	 * @param ctx
	 * @param germibox
	 * @return int O resultado do c�lculo.
	 * @throws Exception
	 */
	private int calculaGermi(PersistenceEvent ctx, ArrayList<Integer> germibox) throws Exception {
		// Calcula a m�dia
		double media = (germibox.get(0) + germibox.get(1) + germibox.get(2) + Double.valueOf(germibox.get(3))) / 4;
		int result = (int) Math.round(media);
		
		// Obt�m a toler�ncia.
		int tolerancia = getTolerancia(ctx, result);
		

		// Se a diferen�a do maior valor com o menor valor for menor que a tolerancia.
		if (germibox.get(3) - germibox.get(0) > tolerancia) {

			// Calcula a m�dia.
			media = (germibox.get(1) + germibox.get(2) + Double.valueOf(germibox.get(3))) / 3;
			result = (int) Math.round(media);

			// Obt�m toler�ncia
			tolerancia = getTolerancia(ctx, result);

			// Se a diferen�a do maior valor com o menor valor for menor que a tolerancia.
			if (germibox.get(3) - germibox.get(1) > tolerancia) 
				throw new Exception("Diferen�a de valores lidos � maior do que a toler�ncia.");
			
		} // end if
		
		return result;
	}
	
	private int calculaGermi3(PersistenceEvent ctx, ArrayList<Integer> germibox) throws Exception {
		int result;
		
		// Calcula a m�dia.
		double media = (germibox.get(0) + germibox.get(1) + Double.valueOf(germibox.get(2))) / 3;
		result = (int) Math.round(media);

		int tolerancia = getTolerancia(ctx, result); // Obt�m toler�ncia

		// Se a diferen�a do maior valor com o menor valor for menor que a tolerancia.
		if (germibox.get(2) - germibox.get(0) > tolerancia) 
			throw new Exception("Diferen�a de valores lidos � maior do que a toler�ncia.");
		
		return result;
	}

}
