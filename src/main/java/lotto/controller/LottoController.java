package lotto.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lotto.domain.lotto.Investment;
import lotto.domain.lotto.LottoBundle;
import lotto.domain.lotto.LottoNumber;
import lotto.domain.lotto.LottoResult;
import lotto.domain.lotto.Rank;
import lotto.domain.lotto.WinningNumbers;
import lotto.domain.utility.Splitter.CustomSplitter;
import lotto.domain.utility.converter.Converter;
import lotto.domain.utility.generator.RandomIntegerListGenerator;
import lotto.domain.utility.sorting.AscendingSorter;
import lotto.view.input.InputView;
import lotto.view.output.OutputView;

public class LottoController {

    private final InputView inputView;
    private final OutputView outputView;
    private final CustomSplitter splitter;
    private final Converter converter;

    public LottoController(InputView inputView, OutputView outputView, CustomSplitter splitter, Converter converter) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.splitter = splitter;
        this.converter = converter;
    }


    public void run() {
        Investment investment = purchaseLotto();
        LottoBundle lottoBundle = generateLottoBundle(investment.getQuantity());
        WinningNumbers winningNumbers = getWinningNumbers();
        LottoResult lottoResult = calculateResults(lottoBundle, winningNumbers);
        printResults(lottoResult, investment);
    }

    private void validateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 입력이 잘못 되었습니다.");
        }
    }

    private Investment purchaseLotto() {
        try {
            outputView.printPurchaseGuide();
            String input = inputView.readLine();
            validateInput(input);
            int cost = converter.convertToInteger(input);

            outputView.printNewLine();

            return new Investment(BigInteger.valueOf(cost));
        } catch (IllegalArgumentException e) {
            outputView.printErrorMessage(e.getMessage());
            return purchaseLotto();
        }
    }

    private LottoBundle generateLottoBundle(int quantity) {
        outputView.printPurchasedAmount(quantity);
        LottoBundle lottoBundle = new LottoBundle(
                new ArrayList<>(),
                new RandomIntegerListGenerator(),
                new AscendingSorter()
        );
        lottoBundle.generate(quantity);
        outputView.printLottoTicket(lottoBundle.getLottoBundle());
        return lottoBundle;
    }

    private WinningNumbers getWinningNumbers() {
        List<LottoNumber> winningNumbers = inputWinningNumbers();
        LottoNumber bonusNumber = inputBonusNumber();
        return new WinningNumbers(winningNumbers, bonusNumber);
    }

    private List<LottoNumber> inputWinningNumbers() {
        try {
            outputView.printWinningNumbersGuide();
            String input = inputView.readLine();
            validateInput(input);
            outputView.printNewLine();

            return convertToLottoNumbers(input);
        } catch (IllegalStateException e) {
            outputView.printErrorMessage(e.getMessage());
            return inputWinningNumbers();
        }
    }

    private List<LottoNumber> convertToLottoNumbers(String input) {
        String[] splitInput = splitter.splitFrom(input);
        List<LottoNumber> numbers = new ArrayList<>();
        for (String splitNumber : splitInput) {
            numbers.add(new LottoNumber(converter.convertToInteger(splitNumber)));
        }
        return numbers;
    }

    private LottoNumber inputBonusNumber() {
        try {
            outputView.printBonusNumberGuide();
            LottoNumber bonusNumber = new LottoNumber(converter.convertToInteger(inputView.readLine()));
            outputView.printNewLine();
            return bonusNumber;
        } catch (IllegalStateException e) {
            outputView.printErrorMessage(e.getMessage());
            return inputBonusNumber();
        }
    }

    private LottoResult calculateResults(LottoBundle lottoBundle, WinningNumbers winningNumbers) {
        outputView.printWinningStatistics();
        LottoResult lottoResult = new LottoResult(new EnumMap<>(Rank.class), BigInteger.ZERO);
        lottoResult.calculate(lottoBundle, winningNumbers);
        return lottoResult;
    }

    private void printResults(LottoResult lottoResult, Investment investment) {
        Map<Rank, Integer> rankCount = lottoResult.getRankCount();
        outputView.printWinningResult(rankCount);
        outputView.printProfitRate(lottoResult.calculateReturnRate(investment));
    }
}
