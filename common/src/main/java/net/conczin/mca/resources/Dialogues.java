package net.conczin.mca.resources;

import com.google.gson.JsonElement;
import net.conczin.mca.MCA;
import net.conczin.mca.Config;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.AnalysisResults;
import net.conczin.mca.resources.data.Analysis;
import net.conczin.mca.resources.data.SerializablePair;
import net.conczin.mca.resources.data.dialogue.Actions;
import net.conczin.mca.resources.data.dialogue.Answer;
import net.conczin.mca.resources.data.dialogue.Question;
import net.conczin.mca.resources.data.dialogue.Result;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import com.google.gson.JsonPrimitive;
import net.conczin.mca.entity.interaction.InteractionPredicate;
import net.conczin.mca.entity.interaction.gifts.GiftPredicate;

public class Dialogues extends SimpleJsonResourceReloadListener {
    protected static final ResourceLocation ID = MCA.locate("dialogues");

    private static Dialogues INSTANCE;
    private final Map<String, Question> questions = new HashMap<>();

    public Dialogues() {
        super(Resources.GSON, "dialogues");
        INSTANCE = this;
    }

    public static Dialogues getInstance() {
        return INSTANCE;
    }

    private static @NotNull Analysis getFinalAnalysis(List<Analysis> analysis, Answer answer) {
        Analysis finalAnalysis = new Analysis();
        for (int i = 0; i < analysis.size(); i++) {
            boolean positive = answer.getResults().get(i).getActions().isPositive();
            boolean negative = answer.getResults().get(i).getActions().isNegative();
            for (SerializablePair<String, Integer> value : analysis.get(i).getSummands()) {
                finalAnalysis.add(value.left(), value.right() * (positive ? 1 : negative ? -1 : 0));
            }
        }
        return finalAnalysis;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
        questions.clear();
        data.forEach(this::loadDialogue);
    }

    private void loadDialogue(ResourceLocation identifier, JsonElement element) {
        String id = identifier.getPath().substring(identifier.getPath().lastIndexOf('/') + 1);
        if (!this.checkIsMcaDialogue(element)) {
            MCA.LOGGER.warn("Dialogue {} is not properly formatted, not loading", identifier);
            return;
        }

        Question q = Question.fromJson(id, element.getAsJsonObject());

        // Merge questions to allow injections
        if (this.questions.containsKey(id)) {
            q.merge(this.questions.get(id));
        }
        q.getAnswers().sort(Comparator.comparingInt(Answer::getPriority));

        this.questions.put(id, q);

        if (id.equals("main")) {
            injectAffairOption(q);
            injectChatOption(q);
        }
    }

    private void injectChatOption(Question question) {
        if (!Config.getInstance().enableVillagerChatAI)
            return;

        // Action: Chat
        Actions.Action chatAction = Actions.TYPES.get("chat").parse(new JsonPrimitive("start"));
        List<Actions.Action> actionsList = new ArrayList<>();
        actionsList.add(chatAction);
        Actions actions = new Actions(actionsList, false, false);
        Result result = new Result(actions, 100, Collections.emptyList(), false);

        // Answer: "Chat"
        Answer answer = new Answer("mca.dialogue.chat", Collections.emptyList(), Collections.singletonList(result), 0);
        question.getAnswers().add(answer);
    }

    private void injectAffairOption(Question question) {
        List<net.conczin.mca.entity.interaction.Constraint> constraints = new ArrayList<>();
        constraints.add(net.conczin.mca.entity.interaction.Constraint.HEARTS_500);
        constraints.add(net.conczin.mca.entity.interaction.Constraint.ADULT);
        constraints.add(net.conczin.mca.entity.interaction.Constraint.CAN_PROCREATE);
        constraints.add(net.conczin.mca.entity.interaction.Constraint.IS_ATTRACTED);

        // Success Result (Go to affair question)
        Actions.Action procreateAction = Actions.TYPES.get("next")
                .parse(new JsonPrimitive("affair"));
        Actions.Action procreateBonus = Actions.TYPES.get("positive")
                .parse(new JsonPrimitive(Config.getInstance().affairHeartBonus));
        List<Actions.Action> successActionsList = new ArrayList<>();
        successActionsList.add(procreateAction);
        successActionsList.add(procreateBonus);
        Actions successActions = new Actions(successActionsList, false, false);
        Result successResult = new Result(successActions, 100, Collections.emptyList(), false);

        // Refusal Result (Generic - Random chance)
        Actions.Action refuseAction = Actions.TYPES.get("say")
                .parse(new JsonPrimitive("affair.refuse"));
        Actions.Action refusePenalty = Actions.TYPES.get("negative")
                .parse(new JsonPrimitive(Config.getInstance().affairHeartPenalty));
        List<Actions.Action> refuseActionsList = new ArrayList<>();
        refuseActionsList.add(refuseAction);
        refuseActionsList.add(refusePenalty);
        Actions refuseActions = new Actions(refuseActionsList, false, true); // Negative outcome
        Result refuseResult = new Result(refuseActions, 10, Collections.emptyList(), false);

        // Refusal Result (Crabby Personality - High chance)
        Actions.Action refuseCrabbyAction = Actions.TYPES.get("say")
                .parse(new JsonPrimitive("affair.refuse.crabby"));
        Actions.Action refuseCrabbyPenalty = Actions.TYPES.get("negative")
                .parse(new JsonPrimitive(Config.getInstance().affairHeartPenalty));
        List<Actions.Action> refuseCrabbyActionsList = new ArrayList<>();
        refuseCrabbyActionsList.add(refuseCrabbyAction);
        refuseCrabbyActionsList.add(refuseCrabbyPenalty);
        Actions refuseCrabbyActions = new Actions(refuseCrabbyActionsList, false, true);

        List<InteractionPredicate> crabbyConditions = new ArrayList<>();
        GiftPredicate.Condition crabbyCondition = GiftPredicate.CONDITION_TYPES.get("personality")
                .parse(new JsonPrimitive("crabby"));
        crabbyConditions.add(new InteractionPredicate(500, crabbyCondition, List.of("personality")));

        Result refuseCrabbyResult = new Result(refuseCrabbyActions, 0, crabbyConditions, false);

        List<Result> results = new ArrayList<>();
        results.add(successResult);
        results.add(refuseResult);
        results.add(refuseCrabbyResult);

        Answer answer = new Answer("affair", constraints, results, 0);
        question.getAnswers().add(answer);
        MCA.LOGGER.info("Injected 'affair' option into 'main' dialogue with varied responses");
    }

    private boolean checkIsMcaDialogue(JsonElement element) {
        JsonElement answersElement = element.getAsJsonObject().get("answers");
        return answersElement != null && answersElement.isJsonArray();
    }

    public Question getQuestion(String i) {
        return questions.get(i);
    }

    // selects a specific answer while being in given question
    public void selectAnswer(VillagerEntityMCA villager, ServerPlayer player, String questionId, String answerId) {
        Question question = getQuestion(questionId);
        Answer answer = question.getAnswer(answerId);

        // fetch chances for each result
        int total = 0;
        List<Analysis> analysis = new LinkedList<>();
        for (Result r : answer.getResults()) {
            Analysis a = r.getChances(villager, player);
            analysis.add(a);
            total += Math.max(0, a.getTotal());
        }

        // choose weighted random
        int chosen = -1;
        total = total == 0 ? 0 : villager.getRandom().nextInt(total);
        for (Analysis a : analysis) {
            total -= Math.max(0, a.getTotal());
            chosen++;
            if (total < 0) {
                break;
            }
        }

        Actions chosenActions = answer.getResults().get(chosen).getActions();

        // send analysis (if there is a heart impact at all)
        if (chosenActions.isNegative() || chosenActions.isPositive()) {
            Analysis finalAnalysis = getFinalAnalysis(analysis, answer);
            Network.sendToPlayer(new AnalysisResults(finalAnalysis), player);
        }

        // execute that results actions
        chosenActions.trigger(villager, player);
    }
}
