/*
 * Copyright 2019 Gabor Varadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhuinden.simplestack;

import android.os.Parcel;

import com.zhuinden.simplestack.helpers.HasParentServices;
import com.zhuinden.simplestack.helpers.ServiceProvider;
import com.zhuinden.simplestack.helpers.TestKeyWithScope;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

public class ScopingRegisteredCallbackTest {

    private static abstract class ServiceEvent {
        protected final String name;

        public ServiceEvent(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof ServiceEvent)) return false;
            ServiceEvent that = (ServiceEvent) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "ServiceEvent{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    private static class RegisterEvent extends ServiceEvent {
        public RegisterEvent() {
            super("REGISTER");
        }
    }

    private static class ActiveEvent extends ServiceEvent {
        public ActiveEvent() {
            super("ACTIVE");
        }
    }

    private static class InactiveEvent extends ServiceEvent {
        public InactiveEvent() {
            super("INACTIVE");
        }
    }

    private static class UnregisterEvent extends ServiceEvent {
        public UnregisterEvent() {
            super("UNREGISTER");
        }
    }

    private static class Pair<S, T> {
        private S first;
        private T second;

        private Pair(S first, T second) {
            this.first = first;
            this.second = second;
        }

        public static <S, T> Pair<S, T> of(S first, T second) {
            return new Pair<>(first, second);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }


    @Test
    public void registeredWorks() {
        final List<Pair<Object, ? extends ServiceEvent>> events = new ArrayList<>();

        Backstack backstack = new Backstack();
        backstack.setScopedServices(new ServiceProvider());

        class MyService
                implements ScopedServices.Activated, ScopedServices.Registered {
            private final String id;

            MyService(String id) {
                this.id = id;
            }

            @Override
            public void onServiceActive() {
                events.add(Pair.of((Object) this, new ActiveEvent()));
            }

            @Override
            public void onServiceInactive() {
                events.add(Pair.of((Object) this, new InactiveEvent()));
            }

            @Override
            public void onServiceRegistered() {
                events.add(Pair.of((Object) this, new RegisterEvent()));
            }

            @Override
            public void onServiceUnregistered() {
                events.add(Pair.of((Object) this, new UnregisterEvent()));
            }

            @Override
            public String toString() {
                return "MyService{" +
                        "id=" + id +
                        '}';
            }
        }

        final Object service0 = new MyService("service0");

        final Object serviceShared0123P1P2P3 = new MyService("serviceShared0123P1P2P3");

        backstack.setGlobalServices(GlobalServices.builder()
                .addService("service0", service0)
                .addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3)
                .build());

        final Object service1 = new MyService("service1");
        final Object service2 = new MyService("service2");
        final Object service3 = new MyService("service3");

        final Object serviceShared12 = new MyService("serviceShared12");
        final Object serviceShared13 = new MyService("serviceShared13");
        final Object serviceShared23 = new MyService("serviceShared23");
        final Object serviceShared123 = new MyService("serviceShared123");
        final Object serviceShared1P1 = new MyService("serviceShared1P1");
        final Object serviceShared1P2 = new MyService("serviceShared1P2");
        final Object serviceShared1P3 = new MyService("serviceShared1P3");
        final Object serviceShared2P1 = new MyService("serviceShared2P1");
        final Object serviceShared2P2 = new MyService("serviceShared2P2");
        final Object serviceShared2P3 = new MyService("serviceShared2P3");
        final Object serviceShared3P1 = new MyService("serviceShared3P1");
        final Object serviceShared3P2 = new MyService("serviceShared3P2");
        final Object serviceShared3P3 = new MyService("serviceShared3P3");

        final Object serviceP1 = new MyService("serviceP1");
        final Object serviceP2 = new MyService("serviceP2");
        final Object serviceP3 = new MyService("serviceP3");

        TestKeyWithScope beep = new TestKeyWithScope("scope1") {
            @Override
            public void bindServices(ServiceBinder serviceBinder) {
                assertThat(serviceBinder.getScopeTag()).isEqualTo(getScopeTag());

                serviceBinder.addService("service1", service1);

                serviceBinder.addService("serviceShared12", serviceShared12);
                serviceBinder.addService("serviceShared13", serviceShared13);
                serviceBinder.addService("serviceShared123", serviceShared123);
                serviceBinder.addService("serviceShared1P1", serviceShared1P1);
                serviceBinder.addService("serviceShared1P2", serviceShared1P2);
                serviceBinder.addService("serviceShared1P3", serviceShared1P3);
                serviceBinder.addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3);
            }
        };

        abstract class TestKeyWithExplicitParent extends TestKeyWithScope implements HasParentServices {
            TestKeyWithExplicitParent(String name) {
                super(name);
            }

            protected TestKeyWithExplicitParent(Parcel in) {
                super(in);
            }

            @Override
            public final void bindServices(ServiceBinder serviceBinder) {
                if(name.equals(serviceBinder.getScopeTag())) {
                    bindOwnServices(serviceBinder);
                } else {
                    bindParentServices(serviceBinder);
                }
            }

            abstract void bindParentServices(ServiceBinder serviceBinder);

            abstract void bindOwnServices(ServiceBinder serviceBinder);
        }

        TestKeyWithExplicitParent boop = new TestKeyWithExplicitParent("scope2") {
            @Nonnull
            @Override
            public List<String> getParentScopes() {
                return History.of("parent1", "parent2");
            }

            @Override
            void bindParentServices(ServiceBinder serviceBinder) {
                if("parent1".equals(serviceBinder.getScopeTag())) {
                    serviceBinder.addService("serviceP1", serviceP1);
                    serviceBinder.addService("serviceShared1P1", serviceShared1P1);
                    serviceBinder.addService("serviceShared2P1", serviceShared2P1);
                    serviceBinder.addService("serviceShared3P1", serviceShared3P1);
                    serviceBinder.addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3);
                }
                if("parent2".equals(serviceBinder.getScopeTag())) {
                    serviceBinder.addService("serviceP2", serviceP2);
                    serviceBinder.addService("serviceShared1P2", serviceShared1P2);
                    serviceBinder.addService("serviceShared2P2", serviceShared2P2);
                    serviceBinder.addService("serviceShared3P2", serviceShared3P2);
                    serviceBinder.addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3);
                }
            }

            @Override
            void bindOwnServices(ServiceBinder serviceBinder) {
                serviceBinder.addService("service2", service2);

                serviceBinder.addService("serviceShared12", serviceShared12);
                serviceBinder.addService("serviceShared23", serviceShared23);
                serviceBinder.addService("serviceShared123", serviceShared123);
                serviceBinder.addService("serviceShared2P1", serviceShared2P1);
                serviceBinder.addService("serviceShared2P2", serviceShared2P2);
                serviceBinder.addService("serviceShared2P3", serviceShared2P3);
                serviceBinder.addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3);
            }
        };

        TestKeyWithExplicitParent braap = new TestKeyWithExplicitParent("scope3") {
            @Nonnull
            @Override
            public List<String> getParentScopes() {
                return History.of("parent1", "parent3");
            }

            @Override
            void bindParentServices(ServiceBinder serviceBinder) {
                if("parent1".equals(serviceBinder.getScopeTag())) {
                    serviceBinder.addService("serviceP1", serviceP1);
                    serviceBinder.addService("serviceShared1P1", serviceShared1P1);
                    serviceBinder.addService("serviceShared2P1", serviceShared2P1);
                    serviceBinder.addService("serviceShared3P1", serviceShared3P1);
                    serviceBinder.addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3);
                }
                if("parent3".equals(serviceBinder.getScopeTag())) {
                    serviceBinder.addService("serviceP3", serviceP3);
                    serviceBinder.addService("serviceShared1P3", serviceShared1P3);
                    serviceBinder.addService("serviceShared2P3", serviceShared2P3);
                    serviceBinder.addService("serviceShared3P3", serviceShared3P3);
                    serviceBinder.addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3);
                }
            }

            @Override
            void bindOwnServices(ServiceBinder serviceBinder) {
                serviceBinder.addService("service3", service3);

                serviceBinder.addService("serviceShared13", serviceShared13);
                serviceBinder.addService("serviceShared23", serviceShared23);
                serviceBinder.addService("serviceShared123", serviceShared123);
                serviceBinder.addService("serviceShared3P1", serviceShared3P1);
                serviceBinder.addService("serviceShared3P2", serviceShared3P2);
                serviceBinder.addService("serviceShared3P3", serviceShared3P3);
                serviceBinder.addService("serviceShared0123P1P2P3", serviceShared0123P1P2P3);
            }
        };

        /*                      GLOBAL
         *                                PARENT1
         *                        PARENT2        PARENT3
         *   BEEP               BOOP                 BRAAP
         */
        backstack.setup(History.of(beep, boop, braap));

        StateChanger stateChanger = new StateChanger() {
            @Override
            public void handleStateChange(@Nonnull StateChange stateChange, @Nonnull Callback completionCallback) {
                completionCallback.stateChangeComplete();
            }
        };
        backstack.setStateChanger(stateChanger);

        backstack.goBack();

        backstack.goBack();

        backstack.finalizeScopes();

        backstack.goTo(beep);

        assertThat(events).containsExactly(
                Pair.of(service0, new RegisterEvent()),
                Pair.of(serviceShared0123P1P2P3, new RegisterEvent()),
                Pair.of(service1, new RegisterEvent()),
                Pair.of(serviceShared12, new RegisterEvent()),
                Pair.of(serviceShared13, new RegisterEvent()),
                Pair.of(serviceShared123, new RegisterEvent()),
                Pair.of(serviceShared1P1, new RegisterEvent()),
                Pair.of(serviceShared1P2, new RegisterEvent()),
                Pair.of(serviceShared1P3, new RegisterEvent()),
                Pair.of(serviceP1, new RegisterEvent()),
                Pair.of(serviceShared2P1, new RegisterEvent()),
                Pair.of(serviceShared3P1, new RegisterEvent()),
                Pair.of(serviceP2, new RegisterEvent()),
                Pair.of(serviceShared2P2, new RegisterEvent()),
                Pair.of(serviceShared3P2, new RegisterEvent()),
                Pair.of(service2, new RegisterEvent()),
                Pair.of(serviceShared23, new RegisterEvent()),
                Pair.of(serviceShared2P3, new RegisterEvent()),
                Pair.of(serviceP3, new RegisterEvent()),
                Pair.of(serviceShared3P3, new RegisterEvent()),
                Pair.of(service3, new RegisterEvent()),
                Pair.of(service0, new ActiveEvent()),
                Pair.of(serviceShared0123P1P2P3, new ActiveEvent()),
                Pair.of(serviceP1, new ActiveEvent()),
                Pair.of(serviceShared1P1, new ActiveEvent()),
                Pair.of(serviceShared2P1, new ActiveEvent()),
                Pair.of(serviceShared3P1, new ActiveEvent()),
                Pair.of(serviceP3, new ActiveEvent()),
                Pair.of(serviceShared1P3, new ActiveEvent()),
                Pair.of(serviceShared2P3, new ActiveEvent()),
                Pair.of(serviceShared3P3, new ActiveEvent()),
                Pair.of(service3, new ActiveEvent()),
                Pair.of(serviceShared13, new ActiveEvent()),
                Pair.of(serviceShared23, new ActiveEvent()),
                Pair.of(serviceShared123, new ActiveEvent()),
                Pair.of(serviceShared3P2, new ActiveEvent()),
                Pair.of(serviceP2, new ActiveEvent()),
                Pair.of(serviceShared1P2, new ActiveEvent()),
                Pair.of(serviceShared2P2, new ActiveEvent()),
                Pair.of(service2, new ActiveEvent()),
                Pair.of(serviceShared12, new ActiveEvent()),
                Pair.of(serviceShared13, new InactiveEvent()),
                Pair.of(service3, new InactiveEvent()),
                Pair.of(serviceShared3P3, new InactiveEvent()),
                Pair.of(serviceShared1P3, new InactiveEvent()),
                Pair.of(serviceP3, new InactiveEvent()),
                Pair.of(service3, new UnregisterEvent()),
                Pair.of(serviceShared3P3, new UnregisterEvent()),
                Pair.of(serviceP3, new UnregisterEvent()),
                Pair.of(service1, new ActiveEvent()),
                Pair.of(serviceShared13, new ActiveEvent()),
                Pair.of(serviceShared1P3, new ActiveEvent()),
                Pair.of(serviceShared2P3, new InactiveEvent()),
                Pair.of(serviceShared23, new InactiveEvent()),
                Pair.of(service2, new InactiveEvent()),
                Pair.of(serviceShared3P2, new InactiveEvent()),
                Pair.of(serviceShared2P2, new InactiveEvent()),
                Pair.of(serviceP2, new InactiveEvent()),
                Pair.of(serviceShared3P1, new InactiveEvent()),
                Pair.of(serviceShared2P1, new InactiveEvent()),
                Pair.of(serviceP1, new InactiveEvent()),
                Pair.of(serviceShared2P3, new UnregisterEvent()),
                Pair.of(serviceShared23, new UnregisterEvent()),
                Pair.of(service2, new UnregisterEvent()),
                Pair.of(serviceShared3P2, new UnregisterEvent()),
                Pair.of(serviceShared2P2, new UnregisterEvent()),
                Pair.of(serviceP2, new UnregisterEvent()),
                Pair.of(serviceShared3P1, new UnregisterEvent()),
                Pair.of(serviceShared2P1, new UnregisterEvent()),
                Pair.of(serviceP1, new UnregisterEvent()),
                Pair.of(serviceShared1P3, new InactiveEvent()),
                Pair.of(serviceShared1P2, new InactiveEvent()),
                Pair.of(serviceShared1P1, new InactiveEvent()),
                Pair.of(serviceShared123, new InactiveEvent()),
                Pair.of(serviceShared13, new InactiveEvent()),
                Pair.of(serviceShared12, new InactiveEvent()),
                Pair.of(service1, new InactiveEvent()),
                Pair.of(serviceShared0123P1P2P3, new InactiveEvent()),
                Pair.of(service0, new InactiveEvent()),
                Pair.of(serviceShared1P3, new UnregisterEvent()),
                Pair.of(serviceShared1P2, new UnregisterEvent()),
                Pair.of(serviceShared1P1, new UnregisterEvent()),
                Pair.of(serviceShared123, new UnregisterEvent()),
                Pair.of(serviceShared13, new UnregisterEvent()),
                Pair.of(serviceShared12, new UnregisterEvent()),
                Pair.of(service1, new UnregisterEvent()),
                Pair.of(serviceShared0123P1P2P3, new UnregisterEvent()),
                Pair.of(service0, new UnregisterEvent()),
                // restoration to 'beep'
                Pair.of(service0, new RegisterEvent()),
                Pair.of(serviceShared0123P1P2P3, new RegisterEvent()),
                Pair.of(service1, new RegisterEvent()),
                Pair.of(serviceShared12, new RegisterEvent()),
                Pair.of(serviceShared13, new RegisterEvent()),
                Pair.of(serviceShared123, new RegisterEvent()),
                Pair.of(serviceShared1P1, new RegisterEvent()),
                Pair.of(serviceShared1P2, new RegisterEvent()),
                Pair.of(serviceShared1P3, new RegisterEvent()),
                Pair.of(service0, new ActiveEvent()),
                Pair.of(serviceShared0123P1P2P3, new ActiveEvent()),
                Pair.of(service1, new ActiveEvent()),
                Pair.of(serviceShared12, new ActiveEvent()),
                Pair.of(serviceShared13, new ActiveEvent()),
                Pair.of(serviceShared123, new ActiveEvent()),
                Pair.of(serviceShared1P1, new ActiveEvent()),
                Pair.of(serviceShared1P2, new ActiveEvent()),
                Pair.of(serviceShared1P3, new ActiveEvent())
        );
    }
}
