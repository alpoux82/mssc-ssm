package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.service.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

import static guru.springframework.msscssm.domain.PaymentEvent.*;
import static guru.springframework.msscssm.domain.PaymentState.*;
import static guru.springframework.msscssm.service.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(AUTH)
                .end(PRE_AUTH_ERROR)
                .end(AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(NEW).target(NEW).event(PRE_AUTHORIZE).action(preAuthAction()).guard(paymentIdGuard())
                .and()
                .withExternal().source(NEW).target(PRE_AUTH).event(PRE_AUTH_APPROVED)
                .and()
                .withExternal().source(NEW).target(PRE_AUTH_ERROR).event(PRE_AUTH_DECLINED)
                .and()
                .withExternal().source(PRE_AUTH).target(PRE_AUTH).event(AUTHORIZE).action(authAction()).guard(paymentIdGuard())
                .and()
                .withExternal().source(PRE_AUTH).target(AUTH).event(AUTH_APPROVED)
                .and()
                .withExternal().source(PRE_AUTH).target(AUTH_ERROR).event(AUTH_DECLINED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<PaymentState, PaymentEvent>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("sateChanged(from: %s, to: %s", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    private Guard<PaymentState, PaymentEvent> paymentIdGuard() {
        return context -> context.getMessageHeader(PAYMENT_ID_HEADER) != null;
    }

    private Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            if (new Random().nextInt(10) < 8)
            {
                context.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(PRE_AUTH_APPROVED)
                                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                                .build());
            } else {
                context.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(PRE_AUTH_DECLINED)
                                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                                .build());
            }
        };
    }

    private Action<PaymentState, PaymentEvent> authAction() {
        return context -> {
            if (new Random().nextInt(10) < 3) {
                context.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(AUTH_DECLINED)
                                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                                .build());
            } else {
                context.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(AUTH_APPROVED)
                                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                                .build());
            }
        };
    }
}
